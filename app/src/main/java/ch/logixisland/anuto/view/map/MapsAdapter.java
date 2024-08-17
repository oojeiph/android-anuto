package ch.logixisland.anuto.view.map;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.business.game.HighScores;
import ch.logixisland.anuto.business.game.MapInfo;
import ch.logixisland.anuto.business.game.MapRepository;

class MapsAdapter extends BaseAdapter {

    private static final Map<String, Bitmap> sThumbCache = new HashMap<>();

    private final WeakReference<Activity> mActivityRef;
    private final HighScores mHighScores;
    private final List<MapInfo> mMapInfos;

    MapsAdapter(Activity activity, MapRepository mapRepository, HighScores highScores) {
        mActivityRef = new WeakReference<>(activity);
        mMapInfos = mapRepository.getMapInfos();
        mHighScores = highScores;
    }

    static private class ViewHolder {
        final ImageView img_thumb;
        final TextView txt_name;
        final TextView txt_highscore;

        ViewHolder(View view) {
            img_thumb = view.findViewById(R.id.img_thumb);
            txt_name = view.findViewById(R.id.txt_name);
            txt_highscore = view.findViewById(R.id.txt_highscore);
        }
    }

    @Override
    public int getCount() {
        return mMapInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mMapInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Activity activity = mActivityRef.get();

        if (activity == null) {
            return convertView;
        }

        View mapItemView;

        if (convertView == null) {
            mapItemView = LayoutInflater.from(activity).inflate(R.layout.item_map, parent, false);
        } else {
            mapItemView = convertView;
        }

        Resources resources = activity.getResources();
        MapInfo mapInfo = mMapInfos.get(position);
        ViewHolder viewHolder = new ViewHolder(mapItemView);

        viewHolder.txt_name.setText(resources.getString(mapInfo.getMapNameResId()));

        DecimalFormat fmt = new DecimalFormat("###,###,###,###");
        String highScore = fmt.format(mHighScores.getHighScore(mapInfo.getMapId()));
        viewHolder.txt_highscore.setText(resources.getString(R.string.score) + ": " + highScore);

        if (!sThumbCache.containsKey(mapInfo.getMapId())) {
            MapThumbGenerator generator = new MapThumbGenerator();
            Bitmap thumb = generator.generateThumb(resources, mapInfo.getMapDataResId());
            sThumbCache.put(mapInfo.getMapId(), thumb);
        }

        viewHolder.img_thumb.setImageBitmap(sThumbCache.get(mapInfo.getMapId()));

        return mapItemView;
    }
}
