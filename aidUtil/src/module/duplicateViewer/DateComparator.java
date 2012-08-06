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

import java.util.Comparator;

public class DateComparator implements Comparator<DuplicateEntry>{
	@Override
	public int compare(DuplicateEntry entry1, DuplicateEntry entry2) {
		long date1 = entry1.getLastModified();
		long date2 = entry2.getLastModified();
		
		return (int)(date1-date2);
	}
}