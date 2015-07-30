
$(document).ready(function() {
  // hide the text directions initially
  $('#directions-panel').slideToggle(0);
  $("#slideToggle .ui-btn-text").text("TEXT");
  directionsShowing = false;

  $('#slideToggle').click(function() {
    var btnText;
    var btnIconClassToAdd;
    var btnIconClassToRemove;

    if(directionsShowing) {
      btnText = "Show Text Directions";
      btnIconClassToAdd = "ui-icon-arrow-u";
      btnIconClassToRemove = "ui-icon-arrow-d";
    } else {
      btnText = "Show Map";
      btnIconClassToAdd = "ui-icon-arrow-d";
      btnIconClassToRemove = "ui-icon-arrow-u";
    }

    // this may break with a different version of JQM.
    $("#slideToggle .ui-btn-text").text(btnText);
    $("#slideToggle .ui-icon").addClass(btnIconClassToAdd).removeClass(btnIconClassToRemove);

    $('#directions-panel').slideToggle(400);
    $('#map_canvas').slideToggle(400);
    directionsShowing = !directionsShowing;
    return false;
  });
});

