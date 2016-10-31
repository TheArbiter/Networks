import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private List<Pages> db;
	private int serialId;
	
	public Database() throws IOException {
		File list = new File("eBook-pages");
		if(!list.exists()){
//			System.out.println("Enter if stament 1");
			// if dir is different on System
			list = new File("../eBook-pages"); 
		}
		if(!list.exists()){
//			System.out.println("Enter if stament 2");
			//if in immediate directory
			list = new File(System.getProperty("user.dir"));
		}
		this.db = new ArrayList<Pages>();
		this.serialId = 0;
		
		//fill list with contents of the book
		boolean fileCheck = false;
		for(File f : list.listFiles()){
//			System.out.println("file name is " + f.getName());
			if(f.getName().toLowerCase().contains("_page")){
				fileCheck = true;
//				System.out.println("inserting " + f.getName() + " into db");
				Pages p = create(f);
				db.add(p);
			}
		}
		if(!fileCheck){
			System.err.println("No Ebooks!!");
		} else {
			System.out.println("The database for discussion posts has been intialised");
		}
	}
	
	private Pages create (File f){
		List<String> list = new ArrayList<String>();
		String name = f.getName();
		String book = name.replaceAll("_page[0-9]", "");
		String page = name.replace(book + "_page", "");
		String s = "";
		try {
			BufferedReader buf = new BufferedReader(new FileReader(f));
			while(buf.ready()){
				s = buf.readLine();
//				System.out.println("Creating new page with line " + s);
				list.add(s);
			}
			buf.close();
		}
		catch (Exception e){
			System.out.println("Couldnt create new Page");
		}
		return (new Pages(list,book,page));
	}
	
	public synchronized int generateSerialId(){
		return this.serialId++;
	}
	
	public Pages search(String book, String page){
		return search1(book, Integer.parseInt(page));
	}
	
	public Pages search1(String book, int page){
		for (Pages p: db){
			if(p.getBookName().equals(book) && p.getPageNumber() == page){
				return p;
			}
		}
		return null;
	}
	
	public List<Discussions> getAllDiscussionPosts (){
		List<Discussions> allPosts = new ArrayList<Discussions>();
		for(Pages page : this.db){
			for(LineList line : page.getLines()){
				for(Discussions post : line.getDiscussionPost()){
					allPosts.add(post);
				}
			}
		}
		return allPosts;
	}
	
	public Discussions getMoreRecentPost(){
		Discussions mostRecentPost = getAllDiscussionPosts().get(0);
		for(Discussions post : getAllDiscussionPosts()){
			if(mostRecentPost.getSerialNum() != Math.max(mostRecentPost.getSerialNum(), post.getSerialNum())){
				mostRecentPost = post;
			}
		}
		return mostRecentPost;
	}
	
}
