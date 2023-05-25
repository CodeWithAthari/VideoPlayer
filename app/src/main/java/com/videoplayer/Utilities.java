package com.videoplayer;

import android.content.Context;
import android.widget.Toast;

public class Utilities {
    public static void showMessage(Context _context, String _s) {
        Toast.makeText(_context, _s, Toast.LENGTH_SHORT).show();
    }
}
