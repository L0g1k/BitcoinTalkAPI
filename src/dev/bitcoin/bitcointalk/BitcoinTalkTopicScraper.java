package dev.bitcoin.bitcointalk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.scraper.TopicScraper;


/**
 * Scrapes a single topic, finding its pages / posts.
 * 
 * @author Jason
 *
 */
public class BitcoinTalkTopicScraper extends BitcoinTalkScaperServletBase {

	TopicScraper topicScraper = new TopicScraper(database);
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String topicId = req.getParameter("topicId");
		Topic topic = database.getTopic(topicId, false);
		/*if(topic == null)
			topic = new Topic("Unknown title", topicId);*/ // Uncomment to test topic scraping logic even with an empty database.
		topicScraper.scrapeTopic(topic);
		
	}

	

	
}
