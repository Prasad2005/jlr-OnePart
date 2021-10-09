
var FacetingList = function (data, refined_list, pageName, feedName,  cssId, facetName, baseUrl, localFeed, options) {
	this.data = data;
	this.refined_list = refined_list;
	this.baseUrl = baseUrl;
	this.pageName = pageName;
	this.feedName = feedName;
	this.cssId = cssId;
	this.facetName = facetName;
	this.localFeed = localFeed;
	
	
	if (window.location.search.indexOf('?') != -1)
		this.restrict = '&'+ window.location.search.substring(1, window.location.search.length);
	
	options = options || {};
	if (exa.isDef(options.enablePagination)) {
		this.paginationEnabled = options.enablePagination;
	}
	if (exa.isDef(options.paginationSize)) {
		this.paginationSize = options.paginationSize;
	}
	if (exa.isDef(options.enableFiltering)) {
		this.enableFiltering = options.enableFiltering;
	}

};
exa.inherit(FacetingList, exa.ui.Component);

FacetingList.prototype.paginationEnabled = true;
FacetingList.prototype.enableFiltering = true;
FacetingList.prototype.paginationSize = 10;
FacetingList.prototype.currentPage = 1;
FacetingList.prototype.filtering = '';
FacetingList.prototype.restrict='';
FacetingList.prototype.refined_list='';
FacetingList.prototype.cssId='';

FacetingList.prototype.refineCount=0;


FacetingList.prototype.createDom = function () {
	FacetingList.superClass_.createDom.call(this);

	var input,
	list = exa.dom.createDom('ul'),
	pagination;

	if (this.enableFiltering) {
		input = new exa.ui.Input('text');
		input = new exa.ui.Input('text');
		input.addClass('exa-faceting-list-input');	
		this.addChild(input, this);
		this.input_ = input;
		//OPTION: add google icon...
	}

	this.element_.appendChild(list);

	if (this.paginationEnabled) {
		pagination = new Pagination({ pageSize: this.paginationSize });
		pagination.setVisible(false);
		this.addChild(pagination, true);
		this.pagination_ = pagination;
	}
	this.list_ = list;

	this.refresh();
};

FacetingList.prototype.enterDocument = function () {
	FacetingList.superClass_.enterDocument.call(this);
	if (this.enableFiltering) {
		this.input_.bind('keyup', this.onkeyup_, this);
	}
	if (this.paginationEnabled) {
		this.pagination_.bind('previous', this.previousPage_, this);
		this.pagination_.bind('next', this.nextPage_, this);
	}
};

FacetingList.prototype.previousPage_ = function () {
	this.currentPage--;
	this.refresh();
};

FacetingList.prototype.nextPage_ = function () {
	this.currentPage++;
	this.refresh();
};


FacetingList.prototype.onkeyup_ = function (currentContext, event) {

	clearTimeout(this.filterTimeout_);

	if ( event.which == 13 ) {
		this.filterTimeout_ = setTimeout($.proxy(function () {
			this.onchanged_();
		}, this), 100);
	}
};

FacetingList.prototype.onchanged_ = function () {
	var filtering = this.input_.getValue();
	if (this.filtering != filtering) {
		this.filtering = filtering;
		this.currentPage = 1;
		this.refresh();
	}
};
FacetingList.prototype.iterate = function (data, cat, regExp, depth) {
	var curLength = data.length;
	for (var i = 0; i < cat.children.length ; i ++) {
		this.iterate(data, cat.children[i], regExp, depth+1);		
	}
	cat.depth = depth;
	regExp.lastIndex = 0;
	if (regExp.test(cat.label.toLowerCase()) || curLength < data.length) {
		data.splice(curLength, 0, cat);
	} 
};


FacetingList.prototype.refresh = function () {
	var data = this.data,
	flattenedData = [],
	//filteringRegExp,
	currentPage = this.currentPage,
	paginationEnabled = this.paginationEnabled,
	paginationSize = this.paginationSize,
	list = this.list_,
	facetList = this;

	/*
	 * Adjustment: make Ajax call to Access API, store result in flattenedData array 
	 * 
	 * */

	$.getJSON(this.baseUrl+'ajax/refinePage/'+this.pageName+'/'+this.feedName+'?'+this.restrict, {
		'paginationSize': this.paginationSize,
		'currentPage': this.currentPage,
		'facetName': this.facetName,
		'filtering': this.filtering,
		'restrict': this.restrict,
		'feedName': this.feedName,
		'localFeed': this.localFeed,
	})

	.done(function( response ) {
		flattenedData = response.categories;	

		var startIndex =  0,
		endIndex = paginationSize;

		exa.dom.removeChildren(list);

		this.refineCount = 0;
		
		for (var i = startIndex; i < endIndex && i < flattenedData.length; i ++) {
			list.appendChild(facetList.createCallbackLi_(flattenedData[i]));
		}
		
		if (paginationEnabled) {
			if (flattenedData.length < facetList.pagination_.pageSize_){
				facetList.pagination_.setNumberOfRows(flattenedData.length + ((facetList.currentPage-1)*facetList.pagination_.pageSize_));
				//facetList.pagination_.setPageSize(flattenedData.length);
			}
			else{
				//facetList.pagination_.setPageSize(paginationSize);
				facetList.pagination_.setNumberOfRows(1000000);
			}
			facetList.pagination_.setPage(currentPage-1);
			facetList.pagination_.setVisible(true);
			facetList.pagination_.refresh();
		}
	})		

	.fail(function( jqxhr, textStatus, error ) {
		var err = textStatus + ", " + error;
	})
	.always(function() {
	});

	/*
	 * End of adjustment
	 * 
	 * */

};

FacetingList.prototype.getRefineCount = function () {
	return this.refineCount;
}

FacetingList.prototype.createCallbackLi_ = function (element) {
	
	if (element.state == 'REFINED')
		this.refineCount = this.refineCount+1;

	//In case current category is a sub-category, shifting it right: 12,19, 26,..., 47, 49, 51, ...
	var tabcount = element.path.split('/').length-4;	
	var depthClass = 'depthLevel_' + tabcount;
	

	var li = exa.dom.createDom('li', 'exa-faceting-list-li refinecontainer '+depthClass), cb =  exa.dom.createDom('input', ''),
	aggregation = exa.dom.createDom('span', 'exa-faceting-list-li-aggr');

	
	var locationPath = window.location.pathname.split('/');
	var localPage = locationPath[locationPath.length-1];
	
	var fullFacetNameArray = this.facetName.split('/');
	var shortFacetName = fullFacetNameArray[fullFacetNameArray.length-1];
	
	cb.type = 'checkbox';
	
		
	
	exa.dom.setTextContent(cb, element.name);

	var currentRestrict = this.restrict;

	//strip page n� off URL
	var pageNum = '0';
	var urlParams = currentRestrict.split('&');
	for (var i = 0; i < urlParams.length; i++) {
		if (urlParams[i].indexOf(this.localFeed+".page=") == 0){
			var param = urlParams[i].split('=');
			//pageNum = param[1];
			var pageParam = this.localFeed+".page="+param[1];
			var offsetPage = currentRestrict.indexOf(pageParam);
			if ( offsetPage != -1){
				var sizePageParam 	= pageParam.length;
				var pageCleanRestrict = currentRestrict.substring(0, offsetPage)+currentRestrict.substring(offsetPage+sizePageParam, currentRestrict.length);
				currentRestrict = pageCleanRestrict;			
			}
			break;
		}
	}

	//Checks whether current category is refined.
	var position_refined = $.inArray(element.id, this.refined_list.split(","));
	
	//Cleanup URL from previous (un)refinement parameter related to current facet
	if (element.state == 'REFINED' || position_refined != -1 ) {
		cb.checked="checked";
		
		//Flat facet (no Hierarchy)
		//if (element.id.split('/').length < 3){
			//var refinedParam = '&'+this.localFeed+'.r='+encodeURIComponent('+f/'+shortFacetName+'/'+element.node);
		var refinedParam = '&'+this.localFeed+'.r='+encodeURIComponent(element.id);
		var offset = currentRestrict.indexOf(refinedParam);
		if ( offset != -1){
			var size 	= refinedParam.length;
			var cleanRestrict = currentRestrict.substring(0, offset)+currentRestrict.substring(offset+size, currentRestrict.length);
			currentRestrict = cleanRestrict;			
		}
		//}
		//else	
		//label = exa.dom.createDom('a', 'exa-faceting-list-li-refined');		
		label = exa.dom.createDom('a', '');		
		//label.href = localPage+'?'+this.localFeed+'.zr='+encodeURIComponent('f/'+fullFacetNameArray[fullFacetNameArray.length-1]+'/'+element.node)+currentRestrict;
		label.href = localPage+'?'+this.localFeed+'.zr='+encodeURIComponent(element.id)+currentRestrict;
		cb.setAttribute('data-url', label.href);
	}else{
		//var unrefinedParam = '&'+this.localFeed+'.zr='+encodeURIComponent('f/'+shortFacetName+'/'+element.node);
		var unrefinedParam = '&'+this.localFeed+'.zr='+encodeURIComponent(element.id);

		var offset = currentRestrict.indexOf(unrefinedParam);
		if ( offset != -1){
			var size = unrefinedParam.length;
			var cleanRestrict = currentRestrict.substring(0, offset)+currentRestrict.substring(offset+size, currentRestrict.length);
			currentRestrict = cleanRestrict;
		}
		//else	
		
		label = exa.dom.createDom('a', '');
		//label.href = localPage+'?'+this.localFeed+'.r='+encodeURIComponent('+f/'+fullFacetNameArray[fullFacetNameArray.length-1]+'/'+element.node)+currentRestrict;
		label.href = localPage+'?'+this.localFeed+'.r='+encodeURIComponent(element.id)+currentRestrict;
		cb.setAttribute('data-url', label.href);
	}

	label.title = element.name;
	
	exa.dom.setTextContent(label, element.name);

	li.appendChild(cb);
	li.appendChild(label);
	
	return li;
};



function createLi_ (element) {
	var li = exa.dom.createDom('li', 'exa-faceting-list-li'),
	aggregation = exa.dom.createDom('span', 'exa-faceting-list-li-aggr');


	var a;
	if (element.url) {
		a = exa.dom.createDom('a', 'exa-faceting-list-li-a');
		a.href = element.url;
	} else {
		a = exa.dom.createDom('span', 'exa-faceting-list-li-a');
	}

	if (element.refined) {
		exa.dom.addClass(li, 'exa-faceting-list-li-refined');
	}
	a.style.cssText = 'padding-left:' + element.depth*5+ 'px;';
	exa.dom.setTextContent(a, element.label);
	exa.dom.setTextContent(aggregation, element.aggregation);

	li.appendChild(aggregation);
	li.appendChild(a);
	return li;
};