var Library = {};

var isGadget = true;

var listDocIds = [];

try {
    var prefs = new gadgets.Prefs();
} catch (error) {
    isGadget = false;
}

// load the page that will display the content of document specified
Library.documentList = function (docRef, page) {
    data = Library.loadContext();

    if (typeof page == 'number') {
        data.page = page;
    }
    if (isGadget) {
        data.limit = prefs.getString("limit");
    } else {
        data.limit = 5;
    }
    // set new value of docRef
    data.docRef = docRef;

    if (isGadget) {
        Library.loadContent(Library.getBasePath() + '/' + "documentList", data);
    } else {
        Library.loadContent("documentListGet", data);
    }
}

Library.confirmDeleteDocument = function (targetRef, targetTitle) {
    if (isGadget) {
        message = prefs.getMsg("label.gadget.library.delete") + ' "' + targetTitle + '"' + prefs.getMsg("label.gadget.library.interrogation.mark");
    } else {
        message = "Delete " + targetTitle + " ?";
    }
    code = 'Library.deleteDocument( \'' + targetRef + '\' );';
    Library.showConfirmationPopup(message, code);
}

// delete specified document from repository
Library.deleteDocument = function (targetRef) {
    data = Library.loadContext();
    data.targetRef = targetRef;
    Library.loadContent(isGadget ? Library.getBasePath() + '/' + "deleteDocument" : "deleteDocument", data, true);
}

Library.confirmPublishDocument = function (targetRef, targetTitle, public) {
    if (isGadget) {
        if (public) {
            message = prefs.getMsg("label.gadget.library.make.public.begining") + ' "' + targetTitle + '" ' + prefs.getMsg("label.gadget.library.make.public.end");
        } else {
            message = prefs.getMsg("label.gadget.library.make.restricted.begining") + ' "' + targetTitle + '" ' + prefs.getMsg("label.gadget.library.make.restricted.end");
        }
    } else {
        if (public) {
            message = "Make the document " + ' "' + targetTitle + " public?";
        } else {
            message = "Restrict the document " + targetTitle + " to the social workspace?";
        }
    }
    code = 'Library.publishDocument( \'' + targetRef + '\', ' + public + ' );';
    Library.showConfirmationPopup(message, code);
}

// publish targetDocument
Library.publishDocument = function (targetRef, public) {
    data = Library.loadContext();
    data.targetRef = targetRef;
    if (typeof public != 'undefined') {
        data.public = public;
    }
    Library.loadContent(isGadget ? Library.getBasePath()  + '/' + "publishDocument" : "publishDocument", data, true);
}

Library.goToDocument = function (documentURL) {
    window.parent.location = documentURL;
}

Library.encode = function (path) {
    var segments = path.split('/');
    for (var i = 0; i < segments.length; i++) {
        segments[i] = encodeURIComponent(segments[i]);
    }
    return segments.join('/');
}

// load navigation info from context form
Library.loadContext = function () {
    context = {};
    jQuery.each(jQuery('[name="contextInfoForm"]').serializeArray(), function (i, field) {
        context[field.name] = field.value;
    });
    return context;
}

Library.loadContent = function (path, data, forcePost) {
    // add language
    if (isGadget) {
        data.lang = prefs.getLang();
        data.documentLinkBuilder = prefs.getString("documentLinkBuilder");
    } else {
        data.lang = "en";
    }
    if (isGadget || forcePost) {
        jQuery.post(
        path, data, Library.contentLoadedHandler);
    } else {
        jQuery.get(
        path, data, Library.contentLoadedHandler);
    }
}

// called when iframe  is loaded ; used for multipart forms ( see create_document_form.ftl)
Library.iframeLoaded = function (iframe) {
    text = jQuery(iframe).contents().find('body').html();
    if (!Library.isEmpty(text)) {
        jQuery.fancybox.close();
        Library.contentLoadedHandler(text);
    }
}

Library.contentLoadedHandler = function (data) {
    // set the new content in "content" element
    jQuery("#content").html(data);

  jQuery(".addPopup").each(function() {
    Library.addPopupBoxTo($(this));
  });

    // intercept forms submit event and add custom behavior
    jQuery("form").submit(

    function (event) {
        event.preventDefault();
        data = jQuery(this).serializeArray();
        if (isGadget) {
            data.push({
                name: 'limit',
                value: prefs.getString("limit")
            });
        } else {
            data.push({
                name: 'limit',
                value: 5
            });
        }
        Library.loadContent(jQuery(this).attr("action"), data);
    });

    // add the language parameter to all links
    if (isGadget) {
        l = prefs.getLang();
    } else {
        l = "en";
    }
    jQuery("a").attr('href', function (i, h) {
        if (typeof h != 'undefined') {
            if (h.indexOf("javascript") == 0) { // don't add language to href starting with javascript
                return h;
            } else {
                return h + (h.indexOf('?') != -1 ? "&lang=" : "?lang=") + l;
            }
        }
    });

    // remove "alt" attribute from images to avoid be displayed in IE
    if (jQuery.browser.msie) {
        jQuery("img").removeAttr("alt");
    }
    if (isGadget) {
        gadgets.window.adjustHeight();
    }
}

// return the path to access social webengine module
Library.getBasePath = function () {
    return basePath = NXGadgetContext.clientSideBaseUrl + 'site/social';
}

// display an confirmation dialog
// message - message that will be displayed
// code - code that will be executed(as string) if ok button is pressed
Library.showConfirmationPopup = function (message, code) {
    t = '<div class="fancyContent">';
    t += '<div class="fancyMessage">' + message + '</div>';
    t += '<div class="center">';
    t += '<button class="border" name="ok" type="button" onclick="jQuery.fancybox.close();' + code + '">OK</button>';
    t += '<button class="border" name="cancel" type="button" onclick="jQuery.fancybox.close()">Cancel</button>';
    t += '</div>';
    t += '</div>';
    jQuery.fancybox(t, {
        'showCloseButton': false,
        'autoDimensions': false,
        'width': '94%',
        'height': '94%',
        'padding': 0,
        'transitionIn': 'none',
        'transitionOut': 'none'
    });
}

Library.addPopupBoxTo = function (a) {
    jQuery(a).fancybox({
        'width': '100%',
        'height': '100%',
        'autoScale': false,
        'transitionIn': 'none',
        'transitionOut': 'none',
        'type': 'iframe',
        'enableEscapeButton': true,
        'centerOnScroll': true,
        'showCloseButton': false,
        'padding': 0,
        'margin': 0,
        'overlayShow': false
    });
}

// called when gadget is load first time
Library.loadInitialContent = function () {
    Library.documentList(getTargetContextPath());
}

// called when document is ready, loads document comments
Library.documentCommentList = function (docRef) {
    data = Library.loadContext();
    // set new value of docRef
    data.docRef = docRef;
    // Ajax request
    var callFunction = isGadget?Library.getBasePath() + '/' + "documentCommentList":"documentCommentList";
    jQuery.ajax({
        url: callFunction,
        type: "GET",
        data: data,
        async: true,
        success: Library.commentLoadedHandler
    });
}

//DocumentCommentList ajax call success method
Library.commentLoadedHandler = function (response, status, jqXHR) {
    // set comments after related doc row
    $("#" + jqXHR.getResponseHeader("docRef")).after(response);
}

Library.isEmpty = function (s) {
    return (!s || s.length === 0);
}
if (isGadget) {
    gadgets.util.registerOnLoadHandler(Library.loadInitialContent);
}

// Add Comment action handler
Library.addComment = function (docToCommentRef, commentContent, commentParentRef) {
    // set data
    data.docToCommentRef = docToCommentRef;
    data.commentParentRef = commentParentRef;
    // retrieve comment content
    data.commentContent = $(commentContent).val();
    // clear content
    $(commentContent).val("");
    // Ajax request
    var callFunction = isGadget?Library.getBasePath() + '/' + "addComment":"addComment";
    jQuery.ajax({
        url: callFunction,
        type: "POST",
        data: data,
        async: true,
        success: Library.addNewUIComment
    });
}

//Rerender current document comments
Library.addNewUIComment = function (response, status, jqXHR) {
    // set new ui comment depending if an answer or a thread
    var parentCommentRef = jqXHR.getResponseHeader("parentCommentRef");
    if (parentCommentRef != null) {
        //Answer
        $("tr." + parentCommentRef).after(response);
        // Close comment box
        $("#box_" + parentCommentRef).slideToggle(300);
    } else {
        //Thread
        var id = jqXHR.getResponseHeader("docRef");
        // Rerender new comment
        $("#comments_list_" + id).append(response);
        // Close comment box
        $("#box_comment_" + id).slideToggle(300);
        // Display comments list
        if ($("#display_" + id).is(":hidden")) {
            Library.commentsDisplayHandler($("#comment_display_button_" + id), id);
            Library.handleLinkClick("#display_", id);
        }
    }
}

//Like action handler
Library.docLike = function (docRef) {
    // set data
    data.docRef = docRef;
    // Ajax request
    var callFunction = isGadget?Library.getBasePath() + '/' + "docLike":"docLike";
    jQuery.ajax({
        url: callFunction,
        type: "POST",
        data: data,
        async: true,
        success: Library.addLikeUI
    });
}

//Rerender document like number area
Library.addLikeUI = function (response, status, jqXHR) {
    $("a#like_" + jqXHR.getResponseHeader("docRef")).html(response);
}

// Handle Link click event
Library.handleLinkClick = function (divId, docId) {
    $(divId + docId).slideToggle(300);
    $(divId + docId).style.display = true;
    return false;
}

// Switch Show/Hide Comments label
Library.commentsDisplayHandler = function (controller, docId) {
    if ($("#display_" + docId).is(":hidden")) {
        controller.text(prefs.getMsg("label.gadget.library.action.hide.threads"));
    } else {
        controller.text(prefs.getMsg("label.gadget.library.action.show.threads"));
    }
}
