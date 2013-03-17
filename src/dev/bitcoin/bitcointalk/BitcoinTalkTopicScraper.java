package dev.bitcoin.bitcointalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

	private static final int postsPerPage = 20;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String topicId = req.getParameter("topicId");
		Topic topic = database.getTopic(topicId, false);
		if(topic == null)
			topic = new Topic("Unknown title", topicId);
		List<TopicPage> pages = new ArrayList<TopicPage>(topic.getPages());
		TopicPage lastPage = null;
		
		// If a topic is new, start from the beginning. If it's old, then start from the last page and just scrape
		// it from the beginning.
		if(pages.size() > 1) {
			lastPage = pages.get(pages.size()-1);
		}
		
		List<Post> appenginePosts = new ArrayList<Post>();
		final int pageCountWAP = scraper.getPages(topicId);
		
		// Eg. If only 1 page, reset the post count to 0. If 6 pages, start from a post count of 5 * 40 and re-scrape the entire 6th page
		int totalPostsSeen = (topic.getPageCount() - 1) * postsPerPage;
		
		boolean skinny = false;
		if(pageCountWAP > 50 && topic.getPages().isEmpty()) {
			System.out.println("Performing partial download of unseen large topic " + topicId);
			skinny = true;
		}
		
		int wapVsDesktopFactor = postsPerPage / 5; // wap version has 5 posts per page.. we want 40
		
		int wapStartPage = topic.getPageCount() == 0 ? 0 : (topic.getPageCount() - 1) * wapVsDesktopFactor;
		
		for (int i = wapStartPage; i < pageCountWAP; i++) {
			int twoPages = 2*wapVsDesktopFactor;
			boolean startOrEnd = i < twoPages || i > pageCountWAP - twoPages;
			if(!skinny || startOrEnd) {
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
		}
		
		if(pages.size() == 0) {
			createPage(appenginePosts, pages);
		}
		
		topic.postCount = totalPostsSeen;
		topic.setBeingUpdated(false);
		if(lastPage != null) {
			lastPage.delete();
			pages.remove(lastPage);
		}
		topic.setPages(pages);
		topic.save();
		
		
	}
	
	protected void createPage(List<Post> appenginePosts, Collection<TopicPage> pages) {
		final TopicPage topicPage = new TopicPage();
		topicPage.setPosts(appenginePosts);
		topicPage.save();
		pages.add(topicPage);
	}

	
}
