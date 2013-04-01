package dev.bitcoin.bitcointalk.scraper;

import java.util.ArrayList;
import java.util.List;

import dev.bitcoin.bitcointalk.BitcoinTalkWAPScraper;
import dev.bitcoin.bitcointalk.Database;
import dev.bitcoin.bitcointalk.ScraperQueue;
import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Topic;

public class BoardScraper {
	
	protected final BitcoinTalkWAPScraper scraper = new BitcoinTalkWAPScraper();
	/** Be careful using this in here, it could potentially cause an infinite loop */
	ScraperQueue scraperQueue = new ScraperQueue();
	private Database database;
	
	public BoardScraper(Database database) {
		this.database = database;
	}
	
	public Board scrapeBoard(final Board board) {
		return scrapeImpl(board, false);
	}
	
	public Board scrapeBoard(final String boardId, boolean deep) {
		final Board board = database.getBoard(boardId, false);
		return scrapeImpl(board, deep);
	}
	
	private Board scrapeImpl(final Board board, boolean deep) {
		final String boardId = board.boardId;
		final List<dev.bitcoin.bitcointalk.model.Topic> topics = scraper.getTopics(boardId);
		List<Topic> appengineTopics = new ArrayList<Topic>();
		for (dev.bitcoin.bitcointalk.model.Topic topic : topics) {
			final Topic oldTopic = database.getTopic(topic.topicId, false);
			final Topic appEngineTopic = oldTopic == null ? new Topic(topic.title, topic.topicId) : oldTopic;
			appEngineTopic.parentBoardId = boardId;
			appEngineTopic.parentBoardName = board.title;
			appengineTopics.add(appEngineTopic);
			// If the topic is new, scrape it now. 
			if(oldTopic == null && deep) {
				scraperQueue.scrapeTopic(topic.topicId);
			}
		}
		board.setBeingUpdated(false);
		board.save();
		database.saveTopics(board, appengineTopics);
		return board;
	}
}
