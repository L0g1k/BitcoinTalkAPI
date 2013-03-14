package dev.bitcoin.bitcointalk;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Date;
import java.util.List;

import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Category;
import dev.bitcoin.bitcointalk.model.appengine.HasFreshness;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;

public class Database {
	
	ScraperQueue scraperQueue = new ScraperQueue();
	
	public List<Category> getCategories() {
		return ofy().load().type(Category.class).list();
	}
	
	/**
	 * Set checkUpdate to false if you're not sure.
	 * 
	 * @param boardId
	 * @param checkUpdate
	 * @return
	 */
	Board getBoard(String boardId, boolean checkUpdate) {
		final Board board = ofy().load().type(Board.class).filter("boardId", boardId).first().get();
		if(board != null && checkUpdate)
			checkUpdate(board);
		return board;
	}
	/**
	 * Set checkUpdate to false if you're not sure.
	 * 
	 * @param topicId
	 * @param checkUpdate
	 * @return
	 */
	Topic getTopic(String topicId, boolean checkUpdate) {
		final Topic topic = ofy().load().type(Topic.class).filter("topicId", topicId).first().get();
		if(topic != null && checkUpdate)
			checkUpdate(topic);
		return topic;
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
		log("Saving topics list");
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

	

	
	
}
