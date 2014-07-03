package com.krieger.libraries.adapter;

import java.util.ArrayList;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.krieger.libraries.entities.HeadedList;
import com.krieger.libraries.interfaces.AdapterCommand;

public class UniversalListAdapter extends BaseAdapter implements ListAdapter {
	
	private LayoutInflater inflater = null;
	private int rowLayoutId;
	private ArrayList<Object> data;
	private AdapterCommand rowInflationAction;
	
	private ArrayList<Object> dataWithHeaders;
	private ArrayList<Integer> sectionHeaderIndexes;
	private int headerLayoutId;
	private AdapterCommand headerInflationAction;
	boolean isHeaderClickable = false;
	
	private boolean hasSections = false;
	private AdapterCommand[] refreshActions = null;
	private int[] refreshPositions = null;
	private Object[] refreshData = null;
	private int initialSelection = -1;
	private AdapterCommand initAction;
	
	public UniversalListAdapter(LayoutInflater inflater, int rowLayoutId,
			ArrayList<Object> data, AdapterCommand rowInflationAction) {
		this.inflater = inflater;
		this.rowLayoutId = rowLayoutId;
		this.data = data;
		this.rowInflationAction = rowInflationAction;
	}
	
	public UniversalListAdapter(LayoutInflater inflater, int rowLayoutId,
			ArrayList<Object> data, AdapterCommand rowInflationAction,
			int headerLayoutId, AdapterCommand headerInflationAction, boolean isHeaderClickable) {
		this.inflater = inflater;
		this.rowLayoutId = rowLayoutId;
		this.data = data;
		this.rowInflationAction = rowInflationAction;
		this.headerLayoutId = headerLayoutId;
		this.headerInflationAction = headerInflationAction;
		this.isHeaderClickable=isHeaderClickable;
		hasSections=true;
		arrangeData();
	}

	private void arrangeData(){
		dataWithHeaders = new ArrayList<Object>();
		sectionHeaderIndexes = new ArrayList<Integer>();
		int count=-1;
		for(int i=0; i<data.size();i++){
			HeadedList<?, ?> d = (HeadedList<?,?>)data.get(i);
			dataWithHeaders.add(d.getHeader());
			sectionHeaderIndexes.add(++count);
			
			ArrayList<?> x = d.getContents();
			for(int j=0; j<x.size(); j++){
				count++;
				dataWithHeaders.add(x.get(j));
			}
		}
	}
	
	@Override
	public int getCount() {
		if(hasSections)
			return dataWithHeaders.size();
		else
			return data.size();
	}

	@Override
	public Object getItem(int position) {
		if(hasSections)
			return dataWithHeaders.get(position);
		else
			return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if(hasSections){
			if(sectionHeaderIndexes.contains(position))
				return 1;
			else
				return 0;
		}
		else
			return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View res = convertView;
		int layout=rowLayoutId;
		boolean isHeader=false;
		
		if(getItemViewType(position)==1){
			layout=headerLayoutId;
			isHeader=true;
		}
		if (res == null){
        	res = inflater.inflate(layout, null);
		}
		if(refreshActions!=null){
			for(int i=0;i<refreshPositions.length;i++){
				if(position==refreshPositions[i]){
					res=refreshActions[i].execute(refreshData[i], res);
				}else{
					if(initialSelection == position){
						res=initAction.execute(getItem(position), res);
					}else{
						if(!isHeader)
							res=rowInflationAction.execute(getItem(position), res);
						else
							res=headerInflationAction.execute(getItem(position), res);
					}
				}
			}
			if(position==getCount()-1){
				refreshData = null;
				refreshPositions = null;
				refreshActions = null;
			}
		}else{
			if(!isHeader)
				res=rowInflationAction.execute(getItem(position), res);
			else
				res=headerInflationAction.execute(getItem(position), res);
		}
        return res;
	}

	@Override
	public int getViewTypeCount() {
		if(hasSections)
			return 2;
		else
			return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount()<1;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if(getItemViewType(position)==0)
			return true;
		else
			return isHeaderClickable;
	}
	
	public void refreshListItems(AdapterCommand[] actions, Object[] data, int[] positions,
			int initialSelection, AdapterCommand initAction){
		this.refreshActions = actions;
		this.refreshPositions = positions;
		this.refreshData  = data;
		this.initialSelection = initialSelection;
		this.initAction = initAction;
		notifyDataSetChanged();
	}
}
