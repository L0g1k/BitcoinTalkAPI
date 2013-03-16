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

	private static final int postsPerPage = 40;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String topicId = req.getParameter("topicId");
		final Topic topic = database.getTopic(topicId, false);
		List<Post> appenginePosts = new ArrayList<Post>();
		final int pageCount = scraper.getPages(topicId);
		List<TopicPage> pages = new ArrayList<TopicPage>(pageCount);
		
		// If a topic is new, start from the beginning. If it's old, then start from the last page and just scrape
		// it from the beginning.
		
		// Eg. If only 1 page, reset the post count to 0. If 6 pages, start from a post count of 6 * 40.
		int totalPostsSeen = (topic.getPageCount() - 1) * postsPerPage;
		
		if(pageCount > 50) {
			System.out.println("Not downloading this topic " + topicId);
			return;
		}
		int wapVsDesktopFactor = postsPerPage / 5; // wap version has 5 posts per page.. we want 40
		for (int i = (topic.getPageCount() -1) * wapVsDesktopFactor; i < pageCount; i++) {
			System.out.println("Scraping WAP page " + i);
			final int postCount = i*5;
			String page = topicId + "." + postCount;
			final List<dev.bitcoin.bitcointalk.model.Post> posts = scraper.getPosts(page);
			for (dev.bitcoin.bitcointalk.model.Post post : posts) {
				totalPostsSeen++;
				appenginePosts.add(new Post(post.poster, post.content));
			}
			
			if(postCount % postsPerPage == 0) {
				createPage(appenginePosts, pages);
				appenginePosts = new ArrayList<Post>();
			}
		}
		
		if(pages.size() == 0) {
			createPage(appenginePosts, pages);
		}
		
		topic.postCount = totalPostsSeen;
		topic.setBeingUpdated(false);
		topic.setPages(pages);
		topic.save();
		
	}

	protected void createPage(List<Post> appenginePosts, List<TopicPage> pages) {
		final TopicPage topicPage = new TopicPage();
		topicPage.setPosts(appenginePosts);
		topicPage.save();
		pages.add(topicPage);
	}

	
}
