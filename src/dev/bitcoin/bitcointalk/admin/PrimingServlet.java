package dev.bitcoin.bitcointalk.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.bitcoin.bitcointalk.BitcoinTalkScaperServletBase;
import dev.bitcoin.bitcointalk.ScraperQueue;
import dev.bitcoin.bitcointalk.model.appengine.Board;

/**
 * Primes the entire database - useful if you've just cleaned out the database or are
 * starting from scratch. You shouldn't need to run this otherwise, though. The topics and 
 * boards will update themselves automatically after people start requesting them through
 * the API
 * 
 * @author Jason
 *
 */
public class PrimingServlet extends BitcoinTalkScaperServletBase {
	
	/** Be careful using this in here, it could potentially cause an infinite loop */
	ScraperQueue scraperQueue = new ScraperQueue();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		List<Board> boards = database.getAllBoards();
		for (Board board : boards) {
			if(board.getTopics() == null || board.getTopics().isEmpty())
				scraperQueue.scrapeBoard(board.boardId);
		}
	}

}
