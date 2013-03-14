package dev.bitcoin.bitcointalk;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.googlecode.objectify.ObjectifyService;

import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Category;
import dev.bitcoin.bitcointalk.model.appengine.Post;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;
public class ScraperInit implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		ObjectifyService.factory().register(Category.class);
		ObjectifyService.factory().register(Board.class);
		ObjectifyService.factory().register(TopicPage.class);
		ObjectifyService.factory().register(Topic.class);
		ObjectifyService.factory().register(Post.class);
		
		/*Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl("/get-categories-and-boards"));
	    queue.add(withUrl("/get-topics-from-board"));
	    queue.add(withUrl("/get-posts-from-topic"));*/
	}
	
}
