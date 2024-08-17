package ch.logixisland.anuto.view.stats;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.entity.EntityRegistry;
import ch.logixisland.anuto.engine.render.Viewport;
import ch.logixisland.anuto.engine.theme.Theme;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.entity.enemy.EnemyProperties;

public class EnemiesAdapter extends BaseAdapter {
    private final List<Enemy> mEnemies;

    private final WeakReference<Activity> mActivityRef;
    private final Theme mTheme;

    public EnemiesAdapter(Activity activity, Theme theme, EntityRegistry entityRegistry) {
        mActivityRef = new WeakReference<>(activity);
        mTheme = theme;

        mEnemies = new ArrayList<>();
        for (String name : entityRegistry.getEntityNamesByType(EntityTypes.ENEMY)) {
            mEnemies.add((Enemy) entityRegistry.createEntity(name));
        }
    }

    static private class ViewHolder {
        final ImageView img_enemy;
        final TextView txt_name;
        final TextView txt_health;
        final TextView txt_speed;
        final TextView txt_reward;
        final TextView txt_weak_against;
        final TextView txt_strong_against;

        ViewHolder(View view) {
            img_enemy = view.findViewById(R.id.img_enemy);
            txt_name = view.findViewById(R.id.txt_name);
            txt_health = view.findViewById(R.id.txt_health);
            txt_speed = view.findViewById(R.id.txt_speed);
            txt_reward = view.findViewById(R.id.txt_reward);
            txt_weak_against = view.findViewById(R.id.txt_weak_against);
            txt_strong_against = view.findViewById(R.id.txt_strong_against);
        }
    }

    @Override
    public int getCount() {
        return mEnemies.size();
    }

    @Override
    public Object getItem(int position) {
        return mEnemies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Activity activity = mActivityRef.get();

        if (activity == null) {
            return convertView;
        }

        View enemyItemView;

        if (convertView == null) {
            enemyItemView = LayoutInflater.from(activity).inflate(R.layout.item_enemy, parent, false);
        } else {
            enemyItemView = convertView;
        }

        Enemy enemy = mEnemies.get(position);
        EnemyProperties enemyProperties = enemy.getEnemyProperties();

        ViewHolder viewHolder = new ViewHolder(enemyItemView);

        String tmp = activity.getString(enemy.getTextId());
        viewHolder.txt_name.setText(tmp);

        DecimalFormat fmt = new DecimalFormat();
        tmp = fmt.format(enemyProperties.getHealth());
        viewHolder.txt_health.setText(tmp);

        DecimalFormat fmt2 = new DecimalFormat("#0 '%'");
        tmp = fmt2.format(enemyProperties.getSpeed() * 100);
        viewHolder.txt_speed.setText(tmp);

        tmp = fmt.format(enemyProperties.getReward());
        viewHolder.txt_reward.setText(tmp);

        tmp = TextUtils.join("\n", enemyProperties.getWeakAgainst());
        viewHolder.txt_weak_against.setText(!tmp.isEmpty() ? tmp : activity.getString(R.string.none));
        viewHolder.txt_weak_against.setTextColor(mTheme.getColor(R.attr.weakAgainstColor));

        tmp = TextUtils.join("\n", enemyProperties.getStrongAgainst());
        viewHolder.txt_strong_against.setText(!tmp.isEmpty() ? tmp : activity.getString(R.string.none));
        viewHolder.txt_strong_against.setTextColor(mTheme.getColor(R.attr.strongAgainstColor));

        Bitmap bmp = createPreviewBitmap(enemy);
        viewHolder.img_enemy.setImageBitmap(bmp);

        return enemyItemView;
    }

    private Bitmap createPreviewBitmap(Enemy enemy) {
        Viewport viewport = new Viewport();
        viewport.setGameSize(1, 1);
        viewport.setScreenSize(120, 120);

        Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.concat(viewport.getScreenMatrix());
        enemy.drawPreview(canvas);

        return bitmap;
    }

}
