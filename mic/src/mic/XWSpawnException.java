package mic;

import java.util.ArrayList;

public class XWSpawnException extends Exception {

    private ArrayList<String> messages;
    private XWSList newList;
    private XWSList2e newList2e;

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

    public void setNewList2e(XWSList2e newList2e)
    {
        this.newList2e = newList2e;
    }

    public XWSList getNewList()
    {
        return newList;
    }
    public XWSList getNewList2e()
    {
        return newList;
    }
}
