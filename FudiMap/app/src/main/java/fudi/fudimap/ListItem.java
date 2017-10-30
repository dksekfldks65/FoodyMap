package fudi.fudimap;


import android.graphics.drawable.Drawable;

public class ListItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String categoryStr ;
    private int id;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String category) {
        categoryStr = category ;
    }
    public void setId(int id) {this.id=id;}

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.categoryStr ;
    }
    public int getId() {return this.id;}

}