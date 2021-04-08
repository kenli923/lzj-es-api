package com.lzj.utils;

import com.lzj.pojo.Goods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@Component
public class HtmlParseUtil {
    public static void main(String[] args) {
        new HtmlParseUtil().getData("java").forEach(System.out::println);
    }

    public ArrayList<Goods> getData(String keyword) {
        if (keyword != null && keyword.length() > 0) {
            ArrayList<Goods> goods = new ArrayList<>();
            String url = "https://search.jd.com/Search?keyword=" + keyword + "&enc=utf-8";

            try {
                Document document = Jsoup.parse(new URL(url), 30000);
                Element goodsList = document.getElementById("J_goodsList");
                Elements lis = goodsList.getElementsByTag("li");
                for (Element li : lis) {
//                    String img = li.getElementsByTag("img").get(0).attr("data-lazy-img");
//                    String title = li.getElementsByClass("p-name").get(0).text();
//                    String price = li.getElementsByClass("p-price").get(0).text();
//                    System.out.println("title=" + title + "     img=" + img + "    price=" + price);
                    if(li.classNames().contains("gl-item")) {
                        goods.add(new Goods(li.getElementsByClass("p-name").get(0).text()
                                , li.getElementsByClass("p-price").get(0).text()
                                , li.getElementsByTag("img").get(0).attr("data-lazy-img")));
                    }
                }
                return goods;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
