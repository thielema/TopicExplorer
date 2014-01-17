define(function() {
	self = this;
	self.topicexplorerModel = new Object();
	self.topicexplorerModel.loadDocuments = function (parameter,callback) {
		$.getJSON("http://localhost:8080/webapp/JsonServlet?" + parameter.paramString).success(function(receivedParsedJson) {
			self.topicexplorerModel.document = receivedParsedJson.DOCUMENT;
			callback(receivedParsedJson.DOCUMENT_SORTING);
		});
	};
    return self.topicexplorerModel;
});