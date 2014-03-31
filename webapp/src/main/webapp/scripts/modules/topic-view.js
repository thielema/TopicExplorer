define(["knockout", "jquery", "scripts/modules/tab-view"],
function(ko, $) { 	
	self.topicPluginTemplates = topicexplorerModel.config.topicView.pluginTemplates;
	self.topicPluginTemplate = ko.observable(self.topicPluginTemplates[topicexplorerModel.config.topicView.activePlugin]);
	self.selectedTopics = ko.observableArray(topicexplorerModel.data.topicSorting);
	self.changeSelectedTopics = function () { self.selectedTopics(["1"]); };
	self.loadDocumentsForTopic = function (topicId) { 
		topicexplorerModel.loadDocumentsForTab("Command=bestDocs&TopicId="+topicId, "Topic " + topicId);
	};   
	self.leftBodyHeight = ko.observable(100).subscribeTo("leftBodyHeight");
	
	self.topicListHeight = ko.computed (function() {
		return(self.leftBodyHeight() - 90) * 0.3;
	});	
	return self;
});

