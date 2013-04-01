package dev.bitcoin.bitcointalk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.bitcoin.bitcointalk.scraper.BoardScraper;


/**
 * Scrapes a single board, finding its topics.
 * 
 * @author Jason
 *
 */
public class BitcoinTalkBoardScraper extends HttpServlet {
	
	Database database = new Database();
	BoardScraper boardScraper = new BoardScraper(database);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String boardId = req.getParameter("boardId");
		boardScraper.scrapeBoard(boardId, true);
	}

	
}
