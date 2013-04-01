package dev.bitcoin.bitcointalk.scraper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.bitcoin.bitcointalk.BitcoinTalkWAPScraper;
import dev.bitcoin.bitcointalk.Database;
import dev.bitcoin.bitcointalk.model.appengine.Post;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;

public class TopicScraper {
	
	private static final int WAP_POSTS_PER_PAGE = 5; // Observed from the BitcoinTalk WAP interface
	private static final int LARGE_TOPIC_PAGE_LIMIT = 10; // We choose this. It's the number of (desktop) pages which should be considered 'large'
	private static final int POSTS_PER_PAGE = 20; // Observed from the BitcoinTalk desktop interface
	// We're scraping the WAP page, but sometimes we want to think in terms of desktop pages so this helps to convert between them.
	private static final int WAP_DESKTOP_PAGE_FACTOR = POSTS_PER_PAGE / WAP_POSTS_PER_PAGE;
	
	protected final BitcoinTalkWAPScraper scraper = new BitcoinTalkWAPScraper();
	protected final Database database;
	
	public TopicScraper(Database database) {
		this.database = database;
	}

	public Topic scrapeTopic(Topic topic) {
		
		final String topicId = topic.topicId;
		
		List<TopicPage> pages = new ArrayList<TopicPage>(topic.getPages());
		TopicPage lastPage = null;
		
		// If a topic is new, start from the beginning. If it's old, then start from the last page and re-scrape it from there.
		if(pages.size() > 1) {
			lastPage = pages.get(pages.size()-1);
		}
		
		List<Post> appenginePosts = new ArrayList<Post>();
		final int pageCountWAP = scraper.getPages(topicId);
		
		// Eg. If only 1 page, reset the post count to 0. If 6 pages, start from a post count of 5 * 40 and re-scrape the entire 6th page
		int wapStartPage = topic.getPageCount() == 0 ? 0 : (topic.getPageCount() - 1) * WAP_DESKTOP_PAGE_FACTOR;
		int totalPostsSeen = topic.getPageCount() == 0 ? 0 : (topic.getPageCount() - 1) * POSTS_PER_PAGE;
		
		// Implement 'large' topic logic
		boolean skinny = false;
		if(pageCountWAP > LARGE_TOPIC_PAGE_LIMIT * WAP_DESKTOP_PAGE_FACTOR) {
			System.out.println("Performing partial download of unseen large topic " + topicId);
			skinny = true;
		}
		
		// Actually scrape the pages
		for (int i = wapStartPage; i < pageCountWAP; i++) {
			int twoPages = 2 * WAP_DESKTOP_PAGE_FACTOR;
			boolean startOrEnd = i < twoPages || i > pageCountWAP - twoPages;
			final int postCount = i*5;
			if(!skinny || startOrEnd) {
				System.out.println("Scraping WAP page " + i);
				String page = topicId + "." + postCount;
				final List<dev.bitcoin.bitcointalk.model.Post> posts = scraper.getPosts(page);
				for (dev.bitcoin.bitcointalk.model.Post post : posts) {
					totalPostsSeen++;
					appenginePosts.add(new Post(post.poster, post.content));
				}
				
			} else {
				// Fake scraping the WAP page if large topic mode
				totalPostsSeen+=5;
			}
			if(postCount % POSTS_PER_PAGE == 0) {
				createPage(appenginePosts, pages);
				appenginePosts = new ArrayList<Post>();
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
		
		return topic;
	}
	
	protected void createPage(List<Post> appenginePosts, Collection<TopicPage> pages) {
		final TopicPage topicPage = new TopicPage();
		topicPage.setPosts(appenginePosts);
		topicPage.save();
		pages.add(topicPage);
	}
}
