package test.sks.com.busapp;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;


public class BaseExpandableAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;

    public BaseExpandableAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_listview, null);
        }
//        ImageView imgListChild = (ImageView) convertView.findViewById(R.id.child_item_icon);
        TextView txtListChild = (TextView) convertView.findViewById(R.id.childtext);
        txtListChild.setText(childText);

        /**TODO  이미지 다르게? **/
        switch (groupPosition) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            default:
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String headerTitle = (String) getGroup(groupPosition);
        View v = convertView;

        if (convertView == null) {

            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.parent_listview, null);
        }
        LinearLayout layoutParent = (LinearLayout) convertView.findViewById(R.id.parentLayout);
//        ImageView imgSelected = (ImageView) convertView.findViewById(R.id.img_selected);
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.parenttext);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        if(isExpanded) {
//            Drawable img = (Drawable) _context.getResources().getDrawable(R.drawable.arrow);
//            imgSelected.setBackground(img);
            layoutParent.setContentDescription(lblListHeader.getText() + "설명 탭 열려있음. 누르면 탭이 닫힙니다.");

            layoutParent.setBackgroundColor(_context.getResources().getColor(R.color.colorDivider2));
        } else {
//            Drawable img = (Drawable) _context.getResources().getDrawable(R.drawable.circle);
//            imgSelected.setBackground(img);
            layoutParent.setContentDescription(lblListHeader.getText() + "설명 탭 닫혀있음. 누르면 탭 아래로 설명 부분이 확장됩니다.");
            layoutParent.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        }



        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}