/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'AidUtil', a collection of maintenance tools for 'Aid'.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package module.duplicateViewer;

import image.SubsamplingImageLoader;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import module.MaintenanceModule;
import net.miginfocom.swing.MigLayout;
import util.LocationTag;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ButtonGroup;

public class ModuleDuplicateViewer extends MaintenanceModule{
	JPanel displayArea, duplicateViewOptions;
	DefaultListModel<Entry> elm = new DefaultListModel<>();
	DefaultListModel<DuplicateGroup> glm = new DefaultListModel<>();
	
	JList<DuplicateGroup> groupList;
	JList<Entry> entryList;
	JScrollPane entryScrollPane, groupScrollPane;
	
	HashMap<String, Path> tagMap = new HashMap<>();
	boolean stop = false;
	
	DatabaseHandler dbHandler;
	
	ListSelectionListener groupSelectionListener = new ListSelectionListener() {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = groupList.getSelectedIndex();
			if(isValidIndex(index)){
				DuplicateGroup group = glm.get(index);
				populateEntryList(group);
				
				Path imagepath = group.getImagepath();
				displayImage(imagepath);
			}
		}
	};
	
	ActionListener selectListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int selections[] = entryList.getSelectedIndices();

			for (int index : selections) {
				Entry entry = elm.get(index);
				toggleSelection(entry);
			}
			
			entryList.repaint();
		}
	};
	
	private JRadioButton groupFilterVisible;
	private JRadioButton groupFilterValid;
	private JButton entrySelect;
	private JRadioButton groupFilterAll;
	private JLabel lblGroupFilter;
	private final ButtonGroup filterGroup = new ButtonGroup();
	
	private boolean isValidIndex(int index) {
		return (index >= 0);
	}

	private void toggleSelection(Entry entry) {
		if (entry.isSelected()) {
			entry.setSelected(false);
		} else {
			entry.setSelected(true);
		}
	}

	private void populateEntryList(DuplicateGroup group) {
		elm.removeAllElements();
		
		LinkedList<Entry> allEntries = new LinkedList<>();
		allEntries.addAll(group.getSelected());
		allEntries.addAll(group.getNotSelected());
		
		for(Entry entry : allEntries) {
			elm.addElement(entry);
		}
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void optionPanel(Container container) {
		duplicateViewOptions = new JPanel();
		displayArea = new JPanel();
		
		groupList = new JList<>(glm);
		entryList = new JList<>(elm);
		entryScrollPane = new JScrollPane(entryList);
		groupScrollPane = new JScrollPane(groupList);
		

		
		
		duplicateViewOptions.setLayout(new MigLayout("", "[70.00][200.00][grow]", "[grow][]"));
		displayArea.setLayout(new MigLayout("fill"));

		duplicateViewOptions.add(groupScrollPane, "cell 0 0,grow");
		duplicateViewOptions.add(entryScrollPane, "cell 1 0,grow");
		duplicateViewOptions.add(displayArea, "cell 2 0,grow");
		
		container.add(duplicateViewOptions, "grow, push");
		
		entrySelect = new JButton("Select");
		duplicateViewOptions.add(entrySelect, "cell 0 1,growx");
		
		lblGroupFilter = new JLabel("Group filter:");
		duplicateViewOptions.add(lblGroupFilter, "flowx,cell 1 1");
		
		groupFilterAll = new JRadioButton("All");
		groupFilterAll.setSelected(true);
		filterGroup.add(groupFilterAll);
		duplicateViewOptions.add(groupFilterAll, "cell 1 1");
		
		groupFilterVisible = new JRadioButton("Visible");
		filterGroup.add(groupFilterVisible);
		duplicateViewOptions.add(groupFilterVisible, "cell 1 1");
		
		groupFilterValid = new JRadioButton("Valid");
		filterGroup.add(groupFilterValid);
		duplicateViewOptions.add(groupFilterValid, "cell 1 1");
		
		addListeners();
	}
	
	private void addListeners() {
		groupList.removeListSelectionListener(groupSelectionListener);
		groupList.addListSelectionListener(groupSelectionListener);
		
		entrySelect.removeActionListener(selectListener);
		entrySelect.addActionListener(selectListener);
	}

	@Override
	public void start() {
		stop = false;
		glm.removeAllElements();
		elm.removeAllElements();
		
		discoverTags();
		dbHandler = new DatabaseHandler(getConnectionPool(), tagMap);
		loadDuplicates();
		setStatus("Ready");
	}

	@Override
	public void Cancel() {
		stop = true;
	}
	
	private void discoverTags(){
		File[] roots = File.listRoots();
		
		for(File f : roots){
			LinkedList<String> tags = LocationTag.findTags(f.toPath());
			
			if(! isValidTagList(tags)){
				continue;
			}
			
			tagMap.put(tags.get(0), f.toPath());
		}
	}
	
	private boolean isValidTagList(List<String> tags) {
		int size = tags.size();
		if(size == 1) {
			return true;
		}else{
			return false;
		}
	}
	
	private void displayImage(Path imagepath){
		
		try {
			displayArea.removeAll();
			JLabel imageLabel = SubsamplingImageLoader.loadImage(imagepath, displayArea.getSize());
			displayArea.add(imageLabel);
		} catch (IOException e) {
			unableToDisplayImage("Error accessing image");
		} catch (Exception ex) {
			unableToDisplayImage(ex.getMessage());
		}
		
		displayArea.revalidate();
		displayArea.repaint();
	}

	private void unableToDisplayImage(String message) {
		displayArea.add(new JLabel(message));
	}

	private void loadDuplicates(){
		addToStatusAndLog("Loading duplicates...");		
		LinkedList<Entry> entries = dbHandler.getDuplicates();
		
		addToStatusAndLog("Creating groups...");
		LinkedList<DuplicateGroup> groups = GroupListCreator.createList(entries);
		
		filterGroups(groups);
		
		addToStatusAndLog("Adding groups to GUI...");
		SwingUtilities.invokeLater(new GroupListPopulator(groups));
		
		int duplicateNum = entries.size();
		int groupNum = groups.size();
		
		info(duplicateNum + " duplicates in " + groupNum + " groups.");
	}
	
	private void addToStatusAndLog(String message){
		setStatus(message);
		info(message);	
	}
	
	private void filterGroups(List<DuplicateGroup> groups) {
		final String FILTER_MSG = "Filtering groups...";
		if(groupFilterAll.isSelected()){
			return;
		}else if(groupFilterValid.isSelected()){
			addToStatusAndLog(FILTER_MSG);
			GroupFilter.onlyFullyValidGroups(groups);
		}else if(groupFilterVisible.isSelected()){
			addToStatusAndLog(FILTER_MSG);
			GroupFilter.onlyVisibleTagGroups(groups);
		}
	}
	
	class GroupListPopulator implements Runnable {
		List<DuplicateGroup> groups;
		
		public GroupListPopulator(List<DuplicateGroup> groups) {
			this.groups = groups;
		}

		@Override
		public void run() {
			for (DuplicateGroup group : groups) {
				glm.addElement(group);
			}
		}
	}
}
