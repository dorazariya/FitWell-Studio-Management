package entity;

public class Tip {
	//Fields
	private int tipID;
	private String content;
	private String URL;
	private int classID;
	
	//Full Constructor
	public Tip(int tipID, String content, String URL, int classID) {
        this.tipID = tipID;
        this.content = content;
        this.URL = URL;
        this.classID = classID;
    }
	
	//Partial Constructor
    public Tip(String content, String URL) {
        this.content = content;
        this.URL = URL;
    }

    //Getters
	public int getTipID() {
		return tipID;
	}

	public String getContent() {
		return content;
	}

	public String getURL() {
		return URL;
	}

	public int getClassID() {
		return classID;
	}

	//Setters
	public void setTipID(int tipID) {
		this.tipID = tipID;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public void setClassID(int classID) {
		this.classID = classID;
	}
}