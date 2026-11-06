package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.api.SingleModInstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Lists the mods in the "mods" folder of the selected profile.
 * Tap = enable/disable (renames X.jar <-> X.jar.disabled), long tap = delete.
 */
public class InstalledModsFragment extends Fragment {
    public static final String TAG = "InstalledModsFragment";
    private static final String DISABLED_SUFFIX = ".disabled";

    private final List<File> mMods = new ArrayList<>();
    private ModsAdapter mAdapter;
    private TextView mEmptyView;
    private TextView mFolderView;

    public InstalledModsFragment() {
        super(R.layout.fragment_installed_mods);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView list = view.findViewById(R.id.installed_mods_list);
        mEmptyView = view.findViewById(R.id.installed_mods_empty);
        mFolderView = view.findViewById(R.id.installed_mods_folder);
        mAdapter = new ModsAdapter(requireContext());
        list.setAdapter(mAdapter);
        list.setEmptyView(mEmptyView);
        list.setOnItemClickListener((parent, v, position, id) -> toggle(mMods.get(position)));
        list.setOnItemLongClickListener((parent, v, position, id) -> {
            confirmDelete(mMods.get(position));
            return true;
        });
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) refresh();
    }

    private File getModsDir() {
        return new File(SingleModInstaller.getCurrentProfileDir(), "mods");
    }

    private void refresh() {
        File dir = getModsDir();
        mFolderView.setText(dir.getAbsolutePath());
        mMods.clear();
        File[] files = dir.listFiles((d, name) -> {
            String n = name.toLowerCase(Locale.ROOT);
            return n.endsWith(".jar") || n.endsWith(".jar" + DISABLED_SUFFIX);
        });
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
            mMods.addAll(Arrays.asList(files));
        }
        mAdapter.notifyDataSetChanged();
    }

    private static boolean isDisabled(File f) {
        return f.getName().endsWith(DISABLED_SUFFIX);
    }

    private void toggle(File mod) {
        String name = mod.getName();
        String newName = isDisabled(mod)
                ? name.substring(0, name.length() - DISABLED_SUFFIX.length())
                : name + DISABLED_SUFFIX;
        File target = new File(mod.getParentFile(), newName);
        if (target.exists() || !mod.renameTo(target)) {
            Toast.makeText(requireContext(), R.string.installed_mods_error, Toast.LENGTH_SHORT).show();
        }
        refresh();
    }

    private void confirmDelete(File mod) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.installed_mods_delete_title)
                .setMessage(getString(R.string.installed_mods_delete_message, displayName(mod)))
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    if (!mod.delete())
                        Toast.makeText(requireContext(), R.string.installed_mods_error, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static String displayName(File f) {
        String n = f.getName();
        if (n.endsWith(DISABLED_SUFFIX)) n = n.substring(0, n.length() - DISABLED_SUFFIX.length());
        if (n.toLowerCase(Locale.ROOT).endsWith(".jar")) n = n.substring(0, n.length() - 4);
        return n;
    }

    private class ModsAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        ModsAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override public int getCount() { return mMods.size(); }
        @Override public Object getItem(int position) { return mMods.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView != null ? convertView
                    : mInflater.inflate(R.layout.item_installed_mod, parent, false);
            File mod = mMods.get(position);
            boolean enabled = !isDisabled(mod);
            TextView name = row.findViewById(R.id.installed_mod_name);
            Switch toggle = row.findViewById(R.id.installed_mod_switch);
            name.setText(displayName(mod));
            name.setAlpha(enabled ? 1f : 0.5f);
            toggle.setChecked(enabled);
            return row;
        }
    }
}
