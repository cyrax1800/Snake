package com.project.snake;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class BoardCell {
    private int BoardStatus;
    private ImageView BoardImage;

    public BoardCell(int boardStatus, ImageView boardImage) {
        this.BoardStatus = boardStatus;
        this.BoardImage = boardImage;
    }

    public void setBoardStatus(int boardStatus) {
        this.BoardStatus = boardStatus;
    }

    public int getBoardStatus() {
        return this.BoardStatus;
    }

    public void setBoardImage(int imageID) {
        BoardImage.setImageResource(imageID);
    }

    public void setBoardImage(Bitmap imageBitmap) {
        BoardImage.setImageBitmap(imageBitmap);
    }
}
