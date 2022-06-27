package com.example.ar_airbrush_second.bluetooth;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;

import androidx.annotation.ColorInt;

public final class TextUtil {

    @ColorInt
    static int caretBackground = 0xff666666;
    // use https://en.wikipedia.org/wiki/Caret_notation to avoid invisible control characters
    static CharSequence toCaretString(CharSequence s, boolean keepNewline) {
        return toCaretString(s, keepNewline, s.length());
    }

    static CharSequence toCaretString(CharSequence s, boolean keepNewline, int length) {
        boolean found = false;
        for (int pos = 0; pos < length; pos++) {
            if (s.charAt(pos) < 32 && (!keepNewline || s.charAt(pos) != '\n')) {
                found = true;
                break;
            }
        }
        if (!found)
            return s;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int pos = 0; pos < length; pos++)
            if (s.charAt(pos) < 32 && (!keepNewline || s.charAt(pos) != '\n')) {
                sb.append('^');
                sb.append((char) (s.charAt(pos) + 64));
                sb.setSpan(new BackgroundColorSpan(caretBackground), sb.length() - 2, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.append(s.charAt(pos));
            }
        return sb;
    }
}
