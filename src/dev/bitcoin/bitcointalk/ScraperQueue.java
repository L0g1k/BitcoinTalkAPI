package dev.bitcoin.bitcointalk;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class ScraperQueue {
	
	final Queue queue = QueueFactory.getDefaultQueue();
	   
	public void scrapeCategories() {
		
	}
	
	public void scrapeBoard(String boardId) {
		log("Updating board " + boardId);
		queue.add(withUrl("/get-topics-from-board").param("boardId", boardId).method(Method.GET));
	}
	
	public void scrapeTopic(String topicId) {
		log("Updating topic " + topicId);
		queue.add(withUrl("/get-posts-from-topic").param("topicId", topicId).method(Method.GET));
	}
	
	private void log(String log) {
		System.out.println(log);
	}
}
