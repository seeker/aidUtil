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
package module;

import java.nio.file.Path;
import java.util.AbstractQueue;

import io.SimpleConcurrentWorker;

public class FileHasher extends SimpleConcurrentWorker<Path, String>{
	public FileHasher() {
		super("FileHasher");
	}
	
	@Override
	protected void work(AbstractQueue<Path> input, AbstractQueue<String> output) {
		while(! isInterrupted()){
			//TODO code me
		}
	}
}
