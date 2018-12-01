package edu.iastate.IDE_AND_A_DREAM.Snake_Multiplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import edu.iastate.IDE_AND_A_DREAM.Snake_Multiplayer.Snake_Object.Snake;

public class SnakeView extends View{


    private Paint mPaint = new Paint();

    private TileType SnakeViewMap[][];

    public SnakeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSnakeViewMap(TileType[][] map){
        this.SnakeViewMap = map;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(SnakeViewMap != null){
            float tileSizeX = canvas.getWidth() / SnakeViewMap.length;
            float tileSizeY = canvas.getWidth() / SnakeViewMap[0].length;

            float circleSize = Math.min(tileSizeX,tileSizeY)/2;

            for (int x = 0; x < SnakeViewMap.length ; x++){
                for (int y = 0; y < SnakeViewMap[x].length ; y++) {
                    switch (SnakeViewMap[x][y]){
                        case Nothing:
                            mPaint.setColor(Color.BLACK);
                            break;
                        case Wall:
                            mPaint.setColor(Color.GREEN);
                            break;
                        case SnakeHead:
                            mPaint.setColor(Color.RED);
                            break;
                        case SnakeTail:
                            mPaint.setColor(Color.GREEN);
                            break;
                        case Apple:
                            mPaint.setColor(Color.WHITE);
                            break;
                        case Monster:
                            mPaint.setColor(Color.MAGENTA);
                            break;
                    }
                    canvas.drawCircle(x * tileSizeX +tileSizeX /2f + circleSize/ 2,y * tileSizeY +tileSizeY /2f + circleSize/ 2, circleSize, mPaint);

                }

            }
        }
    }
}
