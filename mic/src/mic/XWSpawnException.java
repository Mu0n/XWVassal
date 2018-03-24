package mic;

import java.util.ArrayList;

public class XWSpawnException extends Exception {

    private ArrayList<String> messages;
    private XWSList newList;

    public XWSpawnException ()
    {
        super();
        messages = new ArrayList<String>();
    }

    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
    }

    public ArrayList<String> getMessages()
    {
        return messages;
    }

    public void addMessage(String message)
    {
        messages.add(message);
    }

    public void setNewList(XWSList newList)
    {
        this.newList = newList;
    }

    public XWSList getNewList()
    {
        return newList;
    }

}
