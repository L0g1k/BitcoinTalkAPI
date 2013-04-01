package dev.bitcoin.bitcointalk;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Date;
import java.util.List;

import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Category;
import dev.bitcoin.bitcointalk.model.appengine.HasFreshness;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;
import dev.bitcoin.bitcointalk.scraper.BoardScraper;
import dev.bitcoin.bitcointalk.scraper.TopicScraper;

public class Database {
	
	ScraperQueue scraperQueue = new ScraperQueue();
	BoardScraper boardScraper = new BoardScraper(this);
	TopicScraper topicScraper = new TopicScraper(this);

	protected final BitcoinTalkWAPScraper scraper = new BitcoinTalkWAPScraper();

	public List<Category> getCategories() {
		return ofy().load().type(Category.class).list();
	}
	
	/**
	 * Get the board contents. In the odd case that there isn't any data, perform a quick
	 * 'in-place' scrape so that the user will get the content. 
	 * 
	 * 
	 * @param boardId 
	 * @param checkUpdate set true to maybe queue a scrape of the board 
	 * contents if they are too old. Set to false if you're not sure
	 * @return
	 */
	public Board getBoard(String boardId, boolean checkUpdate) {
		final Board board = ofy().load().type(Board.class).filter("boardId", boardId).first().get();
		if(board == null) 
			return null;
		
		if(board.isUnripe()) 
			return singleFill(board);
		else if(checkUpdate)
			checkUpdate(board);
		
		return board;
	}
	
	private Board singleFill(Board unripeBoard) {
		return boardScraper.scrapeBoard(unripeBoard);
	}

	/**
	 * Get the topic contents. In the odd case that there isn't any data, perform a quick
	 * 'in-place' scrape so that the user will get the content. 
	 * 
	 * 
	 * @param boardId 
	 * @param checkUpdate set true to maybe queue a scrape of the board 
	 * contents if they are too old. Set to false if you're not sure
	 * @return
	 */
	public Topic getTopic(String topicId, boolean checkUpdate) {
		final Topic topic = ofy().load().type(Topic.class).filter("topicId", topicId).first().get();
		if(topic == null)
			return null;
		
		if(topic.isUnripe())
			return singleFill(topic);
		else if(checkUpdate)
			checkUpdate(topic);
		return topic;
	}
	
	private Topic singleFill(Topic unripeTopic) {
		return topicScraper.scrapeTopic(unripeTopic);
	}
	
	private void checkUpdate(HasFreshness object) {
		if(needsUpdate(object)) {
			log("A " + object.getClass().getCanonicalName() + " is being updated");
			object.setBeingUpdated(true);
			ofy().save().entities(object);
			if(object instanceof Board) 
				scraperQueue.scrapeBoard(object.getId());
			else if(object instanceof Topic) 
				scraperQueue.scrapeTopic(object.getId());
		}
	}

	private boolean needsUpdate(HasFreshness hasFreshness) {
		if(hasFreshness.isBeingUpdated())
			return false;
		final Date lastUpated = hasFreshness.getLastUpated();
		if(lastUpated == null)
			return true;
		
		final Date now = new Date();
		final long timeElapsed = now.getTime() - lastUpated.getTime();
		return timeElapsed > hasFreshness.getFreshnessTime();
	}
	
	public void saveCategories(List<Category> categories) {
		log("Saving category/sub-category list, with " + categories.size() + " entries");
		ofy().save().entities(categories).now();
	}
	
	public void saveTopics(Board board, List<Topic> topics) {
		log("Saving topics list (async)");
		if(board != null) {
			board.setTopics(topics);
			ofy().save().entity(board);
		}
	}
	
	private void log(String log) {
		System.out.println(log);
	}

	private void error(String error) {
		System.err.println(error);
	}

	public TopicPage getPage(String pageId) {
		return ofy().load().type(TopicPage.class).id(Long.valueOf(pageId)).get();
	}

	public List<Board> getAllBoards() {
		return ofy().load().type(Board.class).list();
	}


	

	
	
}
