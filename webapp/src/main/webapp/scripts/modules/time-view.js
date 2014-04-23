define(
    [ "knockout", "jquery", "highstock"],
    function(ko, $) {
    	self.leftBodyHeight=ko.observable(topicexplorerModel.view.leftBodyHeight).subscribeTo("leftBodyHeight");
    	
    	
    	
    	self.timeDesktopHeight= ko.computed(function() {
    		return ((self.leftBodyHeight() - 90) * 0.7);
    	});
    	
    	self.timeScrollCallback = function(el) {
			$("#singleMenuActivator, #singleMenu").css('top', $("#desktop").scrollTop());
		};
		
		self.timeDesktopHeight.subscribe(function() {
			self.makeChart();
		});
		
		
		self.singlePluginTemplates = topicexplorerModel.config.singleView.pluginTemplates;
		self.singlePluginTemplate = ko.observable(self.singlePluginTemplates[topicexplorerModel.config.singleView.activePlugin]);
		self.singlePluginTemplate.subscribe(function(newValue) {
			topicexplorerModel.config.singleView.activePlugin = topicexplorerModel.config.singleView.pluginTemplates.indexOf(newValue);
		});
		
		self.activeTab = ko.observable(topicexplorerModel.view.activeTab)
			.subscribeTo("TabView.activeTab");
		
		self.activeTab.subscribe(function(newValue) {
			if(topicexplorerModel.view.tab[newValue].module == "time-view") {
				self.topicId(topicexplorerModel.view.tab[newValue].focus);
				
				self.makeChart();
			}
		});
		
    	self.topicId = ko.observable(topicexplorerModel.view.tab[topicexplorerModel.view.activeTab].focus);
    	self.makeChart = function() {
    		self.topicId(topicexplorerModel.view.tab[topicexplorerModel.view.activeTab].focus);
    		self.chart = new Highcharts.StockChart({
		   	    chart: {
		    	   renderTo: 'chart',
		    	   height: self.timeDesktopHeight()
		    	},
		    	series: [{
		    	   data: topicexplorerModel.data.topic[self.topicId()].TIME$WORDS_PER_WEEK,
		    	   color: topicexplorerModel.data.topic[self.topicId()].COLOR_TOPIC$COLOR
		    	}]
		    });
    	};
    	
    	self.init = function() {
    		if(typeof topicexplorerModel.data.timeDataLoaded == 'undefined') {
				
				$.getJSON("JsonServlet?Command=getDates").success(function(receivedParsedJson) {
				//	timeData = topicexplorerModel.convertWeek2Date(receivedParsedJson);
					for (key in receivedParsedJson) {
					    $.extend(self.topicexplorerModel.data.topic[key], receivedParsedJson[key]);
					}
					topicexplorerModel.data.timeDataLoaded = 1;
					self.makeChart();
				});
			} else {
				self.makeChart();
			}
    	};
    	return self;
    }); 
