var refinesWidget = {
	state : [],
	initI18N : function(i18n) {
		refinesWidget.i18n = i18n;
	},
	initCheckboxes: function($widget) {
		$widget.bind('click', { _this: this }, function(e) {
			var target = e.data._this.getEventTarget(e);
			if (target != null) {
				if (target.attr('data-url') != null) {
					exa.redirect(target.attr('data-url'));
					return false;
				}
			}
		});
	},
	setCookie: function (name, collapsed) {
		var cookie = $.cookie('refineWidget'),
			cookieValues,
			i;
		
		if (!cookie) {
			if (collapsed) {
				$.cookie('refineWidget', name);
			}
			return;
		}
		cookieValues = cookie.split(';');
		for (i = 0; i < cookieValues.length; i ++) {
			if (cookieValues[i] == name) {
				if (collapsed) {
					return;
				} else {
					cookieValues.splice(i, 1);
					$.cookie('refineWidget', cookieValues.join(';'));
					return;
				}
			}
		}		
		cookieValues.push(name);
		$.cookie('refineWidget', cookieValues.join(';'));
	},
	getEventTarget: function(e) {
		var target = null;
		try {
			if (!e) e = window.event;
			if (e.target) target = e.target;
			else if (e.srcElement) target = e.srcElement;
			if (target.nodeType == 3) // defeat Safari bug
				target = targ.parentNode;
			// target = $(e.originalTarget || e.target)
			return $(target);
		} catch (e) {
		}
		return null;
	},
	initToggle: function($widget) {
		refinesWidget.loadState($widget);

		$widget.find('h3').bind('click', function(e) {
			var $this = $(this);
			$this.toggleClass('sub-header-collapsable sub-header-collapsed');
			$this.find('>.icon-collapsed,>.icon-collapsable').toggleClass('icon-collapsable icon-collapsed');

			var $table = $this.find('+table.facet');
			$table.toggleClass('table-collapsable table-collapsed');

			refinesWidget.setCookie($this.attr('name'), $table.hasClass('table-collapsed'));
			refinesWidget.showRefinementsInfo($table);
		});
	},
	loadState : function ($widget) {
		var cookie = $.cookie('refineWidget'),
			cookieValues,
			i;
		if (!cookie) {
			return;
		}
		cookieValues = cookie.split(';');		
		
		$widget.find('h3').each(function() {
			var $this = $(this);
			var value = $this.attr('name');
			for (i = 0; i < cookieValues.length; i ++) {
				if (cookieValues[i] == value) {
					refinesWidget.showRefinementsInfo($this.find('+table.facet'));
					return;
				}
			}
		});
	},
	showRefinementsInfo : function($table) {
		if ($table.hasClass('table-collapsed')) {
			var nbRefinements = $table.find('.refined,.excluded,input:checked').length,
				$h3 = $table.prev('h3'),
				$infos = $h3.find('> span.infos');
			$h3.addClass('sub-header-collapsed');
			if (nbRefinements == 0) {
				$infos.hide();
			} else if (nbRefinements == 1) {
				$infos.show();
				$infos.html(nbRefinements + ' ' + refinesWidget.i18n.singular);
			} else {
				$infos.show();
				$infos.html(nbRefinements + ' ' + refinesWidget.i18n.plural);
			}
		} else {
			var $infos = $table.prev('h3').find('> span.infos');
			$infos.hide();
		}
	}
};