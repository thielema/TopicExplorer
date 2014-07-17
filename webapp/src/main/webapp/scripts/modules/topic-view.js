define(["knockout", "jquery"],
function(ko, $) { 	
	$(document).delegate(".topicList", "mouseenter", function(){
		$(this).children("#topicMenuActivator").show();
	}).delegate(".topicList", "mouseleave", function(){
		$(this).children("#topicMenuActivator").hide();
		$(this).children("#topicMenuActivator").next().hide();
		$(this).children("#topicMenuActivator").removeClass("rotate2");
		$(this).children("#topicMenuActivator").addClass("rotate1");
	}).delegate("#topicMenuActivator", 'click', function(e){
		$(this).toggleClass("rotate1 rotate2");
	    $(this).next().toggle('blind');	    	    
	});
	
	var extend = new Array("scripts/modules/extenders/topic-view-time", "scripts/modules/extenders/topic-view-wordtype", "scripts/modules/extenders/topic-view-frame");
	
	var self = {};
	
	self.loadDocumentsForKeywords = function (keyword, context) { 
		var topicId = $(context.target).parents('.topic').attr('id').split('_')[1];
		ko.postbox.publish('openNewTab', {moduleName:"document-browse-tab", tabHeading:"Topic " + topicId + " (" + keyword + ")", data: {topicId: topicId, term: keyword, getParam: "bestDocs&TopicId=" + topicId + "&term=" + keyword}});	
	};
	
	self.TextRepresentation = function(label, field) {
		this.label = label;
		this.field = field;
	};
	self.selectedTopics = ko.observableArray(globalData.TOPIC_SORTING);
	self.Topic = new Array(self.selectedTopics().length);
	
	for(var i=0; i<self.selectedTopics().length; i++) {
		self.Topic[i] = new Object();
		self.Topic[i].TITLE_REPRESENTATION = new Object();
		self.Topic[i].TITLE_REPRESENTATION.ID = i;
		self.Topic[i].TITLE_REPRESENTATION.KEYWORDS = globalData.Term[globalData.Topic[i].Top_Terms[0].TermId].TERM_NAME;
		for(var j = 1; j < 3; j++) {
			self.Topic[i].TITLE_REPRESENTATION.KEYWORDS += ', ' + globalData.Term[globalData.Topic[i].Top_Terms[j].TermId].TERM_NAME;
		}
	}
	self.bodyTemplate = {};
	self.bodyTemplate.ID = 'extenders/topic-keyword';
	self.bodyTemplate.KEYWORDS = 'extenders/topic-keyword';
	self.textSelectArray = ko.observableArray([new self.TextRepresentation('Id', 'ID'), new self.TextRepresentation('Keywords', 'KEYWORDS')]);
	self.textSelection = ko.observable(new self.TextRepresentation('Id', 'ID'));
	self.textSelection.subscribe(function() {
		$('#topicMenuActivator').toggleClass("rotate1 rotate2");
		$('#topicMenu').toggle('blind');	   
	});
	
	self.loadDocumentsForTopic = function (topicId) { 
		ko.postbox.publish('openNewTab', {moduleName:"document-browse-tab", tabHeading:"Topic " + topicId, data: {topicId: topicId, getParam: "bestDocs&TopicId=" + topicId}});
	};   
	
	self.triggerResize = function() {
		$(window).trigger('resize');
	};
	
	self.windowHeight = ko.observable(Math.max(400, $(window).height(), /* For opera: */ document.documentElement.clientHeight)).subscribeTo(
	"windowHeight");
	
	self.topicListHeight = ko.computed (function() {
		return(self.windowHeight() - 154) * 0.3;
	});	
	
	self.termsLoading = ko.observable(false);
	
	self.termScrollCallback = function(el) {
		if(!self.termsLoading() && !globalData.Topic[el].termFull && $('#topic_' + el).children('div').children('.topicElementContent').height() +  $('#topic_' + el).children('div').children('.topicElementContent').scrollTop() >=  $('#topic_' + el).children('div').children('.topicElementContent')[0].scrollHeight - 10) {
			self.termsLoading(true);
			$.getJSON("JsonServlet?Command=getTerms&TopicId=" + el + "&offset=" + globalData.Topic[el].termCount).success(function(receivedParsedJson) {
				$.extend(globalData.Term, receivedParsedJson.Term);
				var termSorting = [];
				if( receivedParsedJson.Topic[el].Top_Terms.length < 20) {
					globalData.Topic[el].termsFull(true);
				} 
				for(var j = 0; j <  receivedParsedJson.Topic[el].Top_Terms.length; j++) {
					termSorting.push(receivedParsedJson.Topic[el].Top_Terms[j].TermId);
					globalData.Topic[el].Top_Terms.push(receivedParsedJson.Topic[el].Top_Terms[j]);
				}
				globalData.Topic[el].termSorting(globalData.Topic[el].termSorting().concat(termSorting));
				globalData.Topic[el].termCount += termSorting.length;	
				self.termsLoading(false);				
			});
		}
	};	
	
	self.getScrollPositionByValue = function (val) {		
		var scroll = val;
		var maxScrollPos = $('.topicPrevElCont').width() - $('.topicPrevSlider').width() - 8;
		var maxScroll = $('.topicList > ul').width() - $('.topicList').width();
		var position = Math.round(scroll*maxScrollPos/maxScroll);
		if(position < 0)
			position = 0;
		if(position > maxScrollPos)
			position = maxScrollPos;
		return position;
	};
	ko.postbox.subscribe("moveToTopic", function(topic) {
		var topic_id = 0;	
		if(!topic.currentTarget) {
			topic_id = topic;
		} else {
			topic_id = $(topic.currentTarget).attr('id').split("_")[1];
		}
		var offset = $(".topicList").width() / 2 - $("#topic_" + topic_id).width() / 2;
		$(".topicList").animate({
			scrollLeft : ($("#topic_" + topic_id).position().left - offset)
		}, {
			queue: false,
			duration : 2000,
			easing : "swing"
		});
		$(".topicPrevSlider").animate({
			left : self.getScrollPositionByValue(($("#topic_" + topic_id).position().left - offset))
		}, {
			queue: false,
			duration : 2000,
			easing : "swing"
		});	
		$("#topicMenuActivator, #topicMenu").css(
			'left', Math.min(Math.max(0, $("#topic_" + topic_id).position().left - offset), $('.topicList > ul').width() -  $('.topicList').width())
		);
	});
	
	for (var i=0;i<extend.length;i++) {
		var extender = require(extend[i]);
		extender(self);
	}
	
	return self;
});

