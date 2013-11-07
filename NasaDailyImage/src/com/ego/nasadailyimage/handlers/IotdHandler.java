package com.ego.nasadailyimage.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ego.nasadailyimage.exceptions.NasaDailyImageException;

public class IotdHandler extends DefaultHandler {
   private String url = "http://www.nasa.gov/rss/image_of_the_day.rss";
   private boolean inUrl = false;
   private boolean inTitle = false;
   private boolean inDescription = false;
   private boolean inItem = false;
   private boolean inDate = false;
   private Bitmap image = null;
   private String title = null;
   private StringBuffer description = new StringBuffer();
   private String date = null;

   public void processFeed() throws NasaDailyImageException {
      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser parser = factory.newSAXParser();
         XMLReader reader = parser.getXMLReader();
         reader.setContentHandler(this);
         InputStream inputStream = new URL(url).openStream();
         reader.parse(new InputSource(inputStream));
      } catch (MalformedURLException e) {
         throw new NasaDailyImageException("The URL is bad: " + url);
      } catch (IOException e) {
         throw new NasaDailyImageException("An IO Exception occured while fetching the NASA feed.");
      } catch (ParserConfigurationException e) {
         throw new NasaDailyImageException("NASA feed could not be parsed correctly.");
      } catch (SAXException e) {
         throw new NasaDailyImageException("NASA feed could not be parsed correctly.");
      }

   }

   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (localName.equals("url")) {
         inUrl = true;
      } else {
         inUrl = false;
      }
      if (localName.startsWith("item")) {
         inItem = true;
      } else if (inItem) {
         if (localName.equals("title")) {
            inTitle = true;
         } else {
            inTitle = false;
         }
         if (localName.equals("description")) {
            inDescription = true;
         } else {
            inDescription = false;
         }
         if (localName.equals("pubDate")) {
            inDate = true;
         } else {
            inDate = false;
         }
      }
   }

   public void characters(char ch[], int start, int length) {
      String chars = new String(ch).substring(start, start + length);
      if (inUrl && url == null) {
         Log.i("IotdHandler", "The URL chars is: " + chars);
         image = getBitmap("http://www.mine-control.com/zack/palette/lake.bmp");

      }
      if (inTitle && title == null) {
         title = chars;
      }
      if (inDescription) {
         description.append(chars);
      }
      if (inDate && date == null) {
         date = chars;
      }
   }

   private Bitmap getBitmap(String url) {
      try {
         Log.i("IotHandler", "Hitting URL: " + url);
         HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
         connection.setDoInput(true);
         connection.connect();
         InputStream input = connection.getInputStream();
         Log.i("IotHandler", "Obtained input stream");
         Bitmap bitmap = BitmapFactory.decodeStream(input);
         Log.i("IotHandler", "Bitmap downloaded");
         input.close();
         return bitmap;
      } catch (IOException ioe) {
         ioe.printStackTrace();
         Log.e("IotHandler", ioe.getMessage());
         return null;
      }
   }

   public Bitmap getImage() {
      return image;
   }

   public String getTitle() {
      return title;
   }

   public String getDescription() {
      return description.toString();
   }

   public String getDate() {
      return date;
   }
}
