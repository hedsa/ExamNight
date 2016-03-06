package ir.mohandesplus.examnight.utils;

import java.util.Map;

public class WebUtils {

    public static String generateCacheKeyWithParam(String url, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url += entry.getKey() + "=" + entry.getValue();
        }
        return url;
    }

    public static String generateUrlWithGetParams(String url, Map<String, String> params) {
        if (params == null) return url;
        if (params.size() == 0) return url;
        url += "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url += entry.getKey() + "=" + entry.getValue() + "&";
        }
        return url;
    }

    public static String generateHtmlFromLaTeXCode(String code) {

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<script type=\"text/x-mathjax-config\">\n" +
                "MathJax.Hub.Config({messageStyle: 'none', tex2jax: {preview: 'none'}});\n" +
                "MathJax.Hub.Config({tex2jax: {inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
                "</script>\n" +
                "<script type=\"text/javascript\"\n" +
                "src=\"file:///android_asset/MathJax/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n" +
                "</script>\n" +
                "<style type=\"text/css\">\n" +
                "div {\n" +
                "direction:rtl;\n" +
                "}\n" +
                "span\n" +
                "{\n" +
                "direction:ltr;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                code +
                "\n</body>\n</html>\n";

    }

}
