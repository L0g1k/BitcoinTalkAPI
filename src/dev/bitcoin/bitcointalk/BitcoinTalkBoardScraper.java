package dev.bitcoin.bitcointalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Topic;


/**
 * Scrapes a single board, finding its topics.
 * 
 * @author Jason
 *
 */
public class BitcoinTalkBoardScraper extends BitcoinTalkScaperServletBase {
	
	/** Be careful using this in here, it could potentially cause an infinite loop */
	ScraperQueue scraperQueue = new ScraperQueue();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String boardId = req.getParameter("boardId");
		final Board board = database.getBoard(boardId, false);
		final List<dev.bitcoin.bitcointalk.model.Topic> topics = scraper.getTopics(boardId);
		List<Topic> appengineTopics = new ArrayList<Topic>();
		for (dev.bitcoin.bitcointalk.model.Topic topic : topics) {
			final Topic oldTopic = database.getTopic(topic.topicId, false);
			final Topic appEngineTopic = oldTopic == null ? new Topic(topic.title, topic.topicId) : oldTopic;
			appEngineTopic.parentBoardId = boardId;
			appEngineTopic.parentBoardName = board.title;
			appengineTopics.add(appEngineTopic);
			// If the topic is new, scrape it now. 
			if(oldTopic == null) {
				scraperQueue.scrapeTopic(topic.topicId);
			}
		}
		board.setBeingUpdated(false);
		board.save();
		database.saveTopics(board, appengineTopics);
	}
}
