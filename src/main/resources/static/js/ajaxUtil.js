// jQuery가 로드될 때까지 대기
(function($) {
    window.ajaxRequest = function(url, method, data, callback) {
        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: url,
            type: method,
            data: JSON.stringify(data),
            contentType: 'application/json',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(result) {
                callback(result);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("오류가 발생했습니다: " + errorThrown);
            }
        });
    };
})(jQuery);
