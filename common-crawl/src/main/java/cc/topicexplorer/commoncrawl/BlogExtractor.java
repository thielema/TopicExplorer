package cc.topicexplorer.commoncrawl;

import static cc.topicexplorer.commoncrawl.HelperUtils.loadFileAsArray;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.hadoop.fs.GlobPattern;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.archive.io.ArchiveReader;

import cc.topicexplorer.commoncrawl.statistics.StatisticsBuilder;

import com.google.common.net.InternetDomainName;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

/**
 * A class for extracting Japanese blogs from ArchiveRecords retrieved from the
 * common crawl web archive.
 * 
 * @author Florian Luecke
 */
public class BlogExtractor {
    private static final Logger     LOG                        = Logger.getLogger(BlogExtractor.class);
    private static final String     RFC_822_DATE_FORMAT_STRING = "E', 'd' 'MMM' 'yy' 'HH':'mm':'ss' 'Z";
    public static final String      VALID_URL_FILE_CONFIG_NAME = "validurlfile";

    private List<StatisticsBuilder> statisticsBuilders         = new ArrayList<StatisticsBuilder>();

    private Pattern                 urlPattern;
    private Pattern                 titlePattern               = Pattern.compile("[\\p{InHiragana}]+");

    public BlogExtractor(Mapper<Text, ArchiveReader, Text, Text>.Context context) throws IOException {
        // load valid URLs from config
        String validURLFile = context.getConfiguration().get(VALID_URL_FILE_CONFIG_NAME);
        if (validURLFile != null) {
            LOG.info("Found blog provider list in: " + validURLFile);
        } else {
            LOG.warn("No blog provider list found!");
            throw new IOException("No blog provider list found!");
        }
        List<String> lines = loadFileAsArray(validURLFile,
                                             context.getConfiguration());

        // create a globbing pattern from the configuration
        String pattern = "{http,https}://{" + HelperUtils.join(lines, ",") + "}";
        this.urlPattern = GlobPattern.compile(pattern);
        LOG.info("Compiled pattern: " + this.urlPattern);
    }

    /**
     * Extracts extracts blog posts from a RecordWrapper.
     * 
     * @param wrapper
     *            a RecordWrapper in which to search for blog posts.
     * @param context
     *            the context of the current mapper.
     * @throws IOException
     *             if writing to the context fails.
     * @throws InterruptedException
     *             if writing to the context fails.
     */
    public void extract(RecordWrapper wrapper,
                        Mapper<Text, ArchiveReader, Text, Text>.Context context)
        throws IOException, InterruptedException {
        // precondition: urlPattern != null, titlePattern != null
        if (this.urlPattern == null || this.titlePattern == null) {
            throw new IllegalStateException("URL Pattern should not be null");
        }

        String url = wrapper.getHeader().getUrl();
        if (url == null) {
            // this should not happen as we filter out eveything but responses,
            // which always have a url
            throw new RuntimeException("URL should not be null");
        }
        Matcher urlMatcher = this.urlPattern.matcher(url);
        if (urlMatcher == null) {
            throw new RuntimeException("Can't create matcher for url: " + url);
        }

        if (urlMatcher.matches()) {
            // the page url is valid

            InternetDomainName domainName = InternetDomainName.from(url);
            String host = domainName.topPrivateDomain().toString();
            StringReader reader = new StringReader(wrapper.getHTTPBody());
            SyndFeedInput in = new SyndFeedInput();
            try {
                SyndFeed feed = in.build(reader);
                for (SyndEntry entry : feed.getEntries()) {

                    String title = entry.getTitle();
                    if (title == null) {
                        title = "";
                    }
                    Matcher titleMatcher = this.titlePattern.matcher(title);
                    if (titleMatcher == null) {
                        throw new RuntimeException("Can't create matcher for title: " + title);
                    }

                    if (titleMatcher.matches()) {
                        String entryUrl = entry.getLink();
                        String mainAuthor = entry.getAuthor();
                        String dateString = getPublishedDateRFC(entry);
                        String contentString = getContents(entry);
                        String[] values = new String[] { entryUrl, mainAuthor,
                                dateString, contentString };

                        StringBuilder builder = new StringBuilder();
                        CSVFormat format = CSVFormat.MYSQL.withHeader("entryUrl",
                                                                      "mainAuthor",
                                                                      "dateString",
                                                                      "contentString");
                        CSVPrinter printer = new CSVPrinter(builder, format);
                        printer.printRecord((Object[]) values);

                        if (contentString.length() != 0) {
                            context.write(new Text(host),
                                          new Text(builder.toString()));
                        }
                        printer.close();
                    } else {
                        LOG.info("Invalid title: " + title);
                    }
                }
                buildStatistics(feed, context);
            } catch (IllegalArgumentException e) {
                LOG.info("No valid feed type at " + url);
            } catch (FeedException e) {
                LOG.error("Feed could not be parsed: " + url);
            }
        } else {
            LOG.info("Invalid URL: " + url);
        }
    }

    /**
     * Get all contents of a SyndEntry as String.
     * 
     * A SyndEntry may contain more than one SyndContent. This returns the
     * values of all SyndContents joined by newlines (\n)
     * 
     * @param entry
     *            the SyndEntry whose contents should be returned.
     * @return a string containing all contents of entry joined by
     *         newlines.
     * @see SyndContent#getValue()
     */
    public static String getContents(SyndEntry entry) {
        // get ATOM content
        List<SyndContent> contents = entry.getContents();
        List<String> contentValues = getValues(contents);
        return HelperUtils.join(contentValues, "\n");
    }

    protected static List<String> getValues(List<SyndContent> contents) {
        List<String> valueList = new ArrayList<String>();
        for (SyndContent syndContent : contents) {
            valueList.add(syndContent.getValue());
        }
        return valueList;
    }

    /**
     * Return the date an entry was published formatted according to <a
     * href="http://www.w3.org/Protocols/rfc822/">RFC822</a>.
     * 
     * @param entry
     *            the SyndEntry whose date to return.
     * @return the requested date.
     * 
     */
    public static String getPublishedDateRFC(SyndEntry entry) {
        Date publishedDate = entry.getPublishedDate();
        return getRFCDate(publishedDate);
    }

    protected static String getRFCDate(Date date) {
        // format the date according to RFC 822
        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC_822_DATE_FORMAT_STRING,
                                                           Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = dateFormat.format(date);
        return dateString;
    }

    /**
     * Notify statistics builders of a new feed.
     * 
     * @param feed
     *            the new Feed.
     * @param context
     *            the context of the current mapper.
     */
    public void buildStatistics(SyndFeed feed,
                                Mapper<Text, ArchiveReader, Text, Text>.Context context) {
        for (StatisticsBuilder statisticsBuilder : this.statisticsBuilders) {
            statisticsBuilder.buildStatistics(feed, context);
        }
    }

    /**
     * Add a statistics builder for feeds to the extractor.
     * 
     * @param sb
     *            the new statistics builder.
     */
    public void addStatisticsBuilder(StatisticsBuilder sb) {
        statisticsBuilders.add(sb);
    }

    /**
     * Remove a statistics builder from the extractor.
     * 
     * @param sb
     *            the statistics builder which to remove.
     */
    public void removeStatisticsBuilder(StatisticsBuilder sb) {
        statisticsBuilders.remove(sb);
    }
}
