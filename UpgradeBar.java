import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.text.rText;

public class UpgradeBar {
    float opacity = 1;
    Vector2 pos;
    float height, rectWidth;
    int strokeWidth;
    int maxRects;
    int curRects = 0;
    Color fillCol;
    String text, smallText;

    int smallTextFontSize;
    float smallTextSpacing;
    Vector2 smallTextDim;

    int textFontSize;
    float textSpacing;
    Vector2 textDim;

    public UpgradeBar(float x, float y, float rectWidth, int maxRects, float height, int strokeWidth, Color fillCol, String text, String smallText) {
        pos = new Vector2(x, y);
        this.height = height;
        this.rectWidth = rectWidth;
        this.maxRects = maxRects;
        this.strokeWidth = strokeWidth;
        this.fillCol = fillCol;
        this.text = text;
        this.smallText = smallText;

        smallTextFontSize = (int)(height * 0.8f);
        smallTextSpacing = -4.f * smallTextFontSize / Graphics.outlineSmallFont.getBaseSize();
        smallTextDim = rText.MeasureTextEx(Graphics.outlineSmallFont, smallText, smallTextFontSize, smallTextSpacing);

        textFontSize = (int)(height * 0.95f);
        textSpacing = -7.f * textFontSize / Graphics.outlineSmallFont.getBaseSize();
        textDim = rText.MeasureTextEx(Graphics.outlineSmallFont, text, textFontSize, textSpacing);
    }

    public void setRects(int curRects) {
        this.curRects = Math.min(curRects, maxRects);
    }

    public void update(float opacity) {
        this.opacity = opacity;
    }

    public void draw() {
        Color strokeCol = Graphics.BAR_GREY;
        Color buttonCol = curRects == maxRects ? Graphics.GREY : fillCol;
        int xInt = (int)pos.x;
        int yInt = (int)pos.y;
        float width = 2 * height * 0.5f + ((maxRects+1)+1) * strokeWidth + (maxRects+1) * rectWidth;
        Graphics.drawRectangleRounded(xInt, yInt, width, height, 1f, 10, Graphics.colAlpha(strokeCol, opacity));
        float higherOpacity = (float) Math.pow(opacity, 0.33);
        if (curRects > 0) {
            Graphics.drawCircleSector(new Vector2(xInt + height * 0.5f, yInt + height * 0.5f), height * 0.5f - strokeWidth, 180, 360, fillCol, higherOpacity);
        }
        float rectX = xInt + height * 0.5f + strokeWidth;
        for (int i = 0; i <= maxRects; i++) {
            Color col = fillCol;
            int xShift = 0, widthShift = 0;
            if (i == 0) {
                xShift = -strokeWidth;
                widthShift = strokeWidth;
            }
            if (i == maxRects) {
                col = buttonCol;
                widthShift = strokeWidth;
            }
            if (i < curRects || i == maxRects) {
                Graphics.drawRectangle(rectX + xShift, yInt + strokeWidth, rectWidth + widthShift, height - 2 * strokeWidth, Graphics.colAlpha(col, higherOpacity));
            }

            rectX += rectWidth + strokeWidth;
        }
        Graphics.drawCircleSector(new Vector2(rectX, yInt + height * 0.5f), height * 0.5f - strokeWidth, 0, 180, buttonCol, higherOpacity);
        Graphics.drawTextCenteredOutline(text, (int) (xInt + (width - height*0.5f - rectWidth) * 0.5f), (int) (yInt + (height) * 0.55f), (int)(height * 0.95f), textSpacing, textDim, Graphics.colAlpha(Color.WHITE, higherOpacity));
        Graphics.drawTextOutline(smallText, new Vector2(rectX - rectWidth*1.5f - strokeWidth - smallTextDim.x * 0.5f, yInt + (height- smallTextDim.y) * 0.6f), (int)(height * 0.8f), smallTextSpacing, Graphics.colAlpha(Color.WHITE, higherOpacity));
    }
}
