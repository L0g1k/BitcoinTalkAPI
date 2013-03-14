package dev.bitcoin.bitcointalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.bitcoin.bitcointalk.model.appengine.Post;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;


/**
 * Scrapes a single topic, finding its pages / posts.
 * 
 * @author Jason
 *
 */
public class BitcoinTalkTopicScraper extends BitcoinTalkScaperServletBase {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String topicId = req.getParameter("topicId");
		final Topic topic = database.getTopic(topicId, false);
		List<Post> appenginePosts = new ArrayList<Post>();
		final int pageCount = scraper.getPages(topicId);
		List<TopicPage> pages = new ArrayList<TopicPage>(pageCount);
		int totalPostsSeen = 0;
		if(pageCount > 50) {
			System.out.println("Not downloading this topic " + topicId);
			return;
		}
		
		for (int i = 0; i < pageCount; i++) {
			final int postCount = i*5;
			String page = topicId + "." + postCount;
			final List<dev.bitcoin.bitcointalk.model.Post> posts = scraper.getPosts(page);
			for (dev.bitcoin.bitcointalk.model.Post post : posts) {
				totalPostsSeen++;
				appenginePosts.add(new Post(post.poster, post.content));
			}
			
			if(postCount %40 == 0) {
				createPage(appenginePosts, pages);
				appenginePosts = new ArrayList<Post>();
			}
		}
		
		if(pages.size() == 0) {
			createPage(appenginePosts, pages);
		}
		
//		final List<dev.bitcoin.bitcointalk.model.Post> posts = scraper.getPosts(topicId);
		
		/*for (dev.bitcoin.bitcointalk.model.Post post : posts) 
			appenginePosts.add(new Post(post.poster, post.content));*/
		topic.postCount = totalPostsSeen;
		topic.setBeingUpdated(false);
		topic.setPages(pages);
		topic.save();
		//database.savePosts(topic, appenginePosts);
	}

	protected void createPage(List<Post> appenginePosts, List<TopicPage> pages) {
		final TopicPage topicPage = new TopicPage();
		topicPage.setPosts(appenginePosts);
		topicPage.save();
		pages.add(topicPage);
	}

	
}
