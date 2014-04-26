package com.kkteam.simplefiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Main activity.
 * 
 * @author Karim Geiger <me@karim-geiger.de>
 * 
 */
public class MainActivity extends Activity {

	public LinearLayout baseLayout;
	private List<FolderListView> listViewArray = new ArrayList<FolderListView>();
	private File sdCardRoot = Environment.getExternalStorageDirectory();
	private String currentPath = "/";
	private Database db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new Database(this.getApplicationContext());

		Intent intent = getIntent();
		String path = intent.getStringExtra(WidgetProvider.EXTRA_WORD);

		if (path != null) {
			// file pressed on widget
			String file = db.getString(WidgetProvider.PREFERENCES_NAME) + "/" + path;
			try {
				// Open file
				openFile(file);
				finish();
			} catch (IsDirectoryException e) {
				// Open directory
				Toast.makeText(this, "Opening folders from widget is currently not supported.", Toast.LENGTH_SHORT).show();
			} catch (FileNotFoundException e) {
				// Invalid file
				Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}

		setContentView(R.layout.activity_main);

		baseLayout = (LinearLayout) findViewById(R.id.llBase);

		// Initiate root view
		onFilePress("/", 0);
	}

	/**
	 * Add new view to base layout.
	 * 
	 * @param folder
	 *            folder containing files
	 * @param location
	 *            view location in layout
	 */
	private void addView(Folder folder, int location) {
		FolderListView lv = new FolderListView(this, folder, listViewArray.size() + 1);

		baseLayout.addView(lv.getView());
		listViewArray.add(lv);
	}

	/**
	 * Open file or folder on file press.
	 * 
	 * @param path
	 *            new file path
	 * @param currentViewLocation
	 *            view location in layout
	 */
	public void onFilePress(String path, int currentViewLocation) {
		try {
			// It's a directory
			Folder folder = new Folder(sdCardRoot, path);
			removeViews(currentViewLocation - 1);
			addView(folder, currentViewLocation - 1);
			currentPath = path;
		} catch (NotADirectoryException e) {
			// It's a file
			try {
				openFile(path);
			} catch (Exception e1) {
				// File does not exist
				Toast.makeText(this, "This file does not exist.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Open file through intent.
	 * 
	 * @param path
	 *            path to file
	 * @throws IsDirectoryException
	 *             file is directory
	 * @throws FileNotFoundException
	 *             file was not found
	 */
	private void openFile(String path) throws IsDirectoryException, FileNotFoundException {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		File file = new File(sdCardRoot, path);

		if (file.isDirectory()) {
			throw new IsDirectoryException();
		}
		if (!file.isFile()) {
			throw new FileNotFoundException();
		}
		try {
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			intent.setDataAndType(Uri.fromFile(file), mimetype);
			startActivity(intent);
		} catch (Exception e) {
			intent.setDataAndType(Uri.fromFile(file), "text/*");
			Toast.makeText(this, "Unknown filetype. Opening as text.", Toast.LENGTH_SHORT).show();
		}
		startActivity(intent);
	}

	/**
	 * Remove view with all views depending on base view.
	 * 
	 * @param location
	 *            location for base view
	 */
	private void removeViews(int location) {
		while (listViewArray.size() - 1 > location) {
			baseLayout.removeViewAt(listViewArray.size() - 1);
			listViewArray.remove(listViewArray.size() - 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_widget) {
			db.setData(WidgetProvider.PREFERENCES_NAME, currentPath);
			Toast.makeText(this, "Widget path is set.", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
