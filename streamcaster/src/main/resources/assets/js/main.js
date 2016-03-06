$(function() {
    $('#type-select').on('change', function() {
        $.get('/sources/config?type=' + $(this).val(), function(data) {
            $('#config').empty();
            $(data).each(function() {
                if (this.type === "select") {
                    var values = "";
                    this.values.map(function(item) {
                        values += '<option>' + item + '</option>';
                    });
                    $('<div class="form-group col-lg-10"><label for="' + this.name + '">' + this.name + '</label>' +
                            '<select name="' + this.name + '" class="form-control" placeholder="' + this.description + '" required="true">' +
                            '<option></option>' +
                            values +
                            '</select>' +
                            '</div>')
                            .appendTo('#config');
                } else {
                    $('<div class="form-group col-lg-10"><label for="' + this.name + '">' + this.name + '</label><input name="' + this.name + '" class="form-control" placeholder="' + this.description + '" required="true"/></div>').appendTo('#config');
                }
            });
        });
    });
    $('.set-profile').each(function() {
        $(this).on('click', function() {
            $('.set-profile').removeClass('active','has-success');
            $('.set-profile > i').removeClass('fa-play');
            $('.set-profile > i').addClass('fa-pause');
            $(this).addClass('active','has-success');
            $(this).find('i').removeClass('fa-pause');
            $(this).find('i').addClass('fa-play');
            ws.send('CHANGE-PROFILE:' + $(this).data('set-profile'));
        })
    });
    $('form').parsley({
        successClass: "has-success",
        errorClass: "has-error",
        classHandler: function(el) {
            return el.$element.closest(".form-group");
        },
        errorsWrapper: '<span class="help-block"></span>',
        errorTemplate: '<span></span>'
    });

})