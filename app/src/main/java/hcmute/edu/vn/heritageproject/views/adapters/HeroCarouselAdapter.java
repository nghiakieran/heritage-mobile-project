package hcmute.edu.vn.heritageproject.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.HeroSlide;

public class HeroCarouselAdapter extends RecyclerView.Adapter<HeroCarouselAdapter.HeroViewHolder> {
    private final List<HeroSlide> slides;
    private final Context context;

    public HeroCarouselAdapter(Context context, List<HeroSlide> slides) {
        this.context = context;
        this.slides = slides;
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hero_carousel, parent, false);
        return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        HeroSlide slide = slides.get(position);
        holder.textTitle.setText(slide.getTitle());
        holder.textSubtitle.setText(slide.getSubTitle());
        Glide.with(context).load(slide.getImage()).into(holder.imageHero);
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class HeroViewHolder extends RecyclerView.ViewHolder {
        ImageView imageHero;
        TextView textTitle, textSubtitle;
        HeroViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHero = itemView.findViewById(R.id.imageHero);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
        }
    }
}
