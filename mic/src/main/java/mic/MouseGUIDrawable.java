package mic;

public abstract class MouseGUIDrawable {
    public int ulX = 0; //upper left corner of the popup
    public int ulY = 0;
    public int totalWidth = 0;
    public int totalHeight = 0;

    public int currentGUIElementBeingEdited = 0;
    public int currentPage = 2; //currently only used for the ship mouse GUI, but could be used by others

}
