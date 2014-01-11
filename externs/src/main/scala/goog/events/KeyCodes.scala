package goog.events

object KeyCodes {
  val MAC_ENTER = 3
  val BACKSPACE = 8
  val TAB = 9
  val NUM_CENTER = 12 // NUMLOCK on FF/Safari Mac
  val ENTER = 13
  val SHIFT = 16
  val CTRL = 17
  val ALT = 18
  val PAUSE = 19
  val CAPS_LOCK = 20
  val ESC = 27
  val SPACE = 32
  val PAGE_UP = 33   // also NUM_NORTH_EAST
  val PAGE_DOWN = 34   // also NUM_SOUTH_EAST
  val END = 35     // also NUM_SOUTH_WEST
  val HOME = 36    // also NUM_NORTH_WEST
  val LEFT = 37    // also NUM_WEST
  val UP = 38      // also NUM_NORTH
  val RIGHT = 39     // also NUM_EAST
  val DOWN = 40    // also NUM_SOUTH
  val PRINT_SCREEN = 44
  val INSERT = 45    // also NUM_INSERT
  val DELETE = 46    // also NUM_DELETE
  val ZERO = 48
  val ONE = 49
  val TWO = 50
  val THREE = 51
  val FOUR = 52
  val FIVE = 53
  val SIX = 54
  val SEVEN = 55
  val EIGHT = 56
  val NINE = 57
  val QUESTION_MARK = 63 // needs localization
  val A = 65
  val B = 66
  val C = 67
  val D = 68
  val E = 69
  val F = 70
  val G = 71
  val H = 72
  val I = 73
  val J = 74
  val K = 75
  val L = 76
  val M = 77
  val N = 78
  val O = 79
  val P = 80
  val Q = 81
  val R = 82
  val S = 83
  val T = 84
  val U = 85
  val V = 86
  val W = 87
  val X = 88
  val Y = 89
  val Z = 90
  val META = 91
  val CONTEXT_MENU = 93
  val NUM_ZERO = 96
  val NUM_ONE = 97
  val NUM_TWO = 98
  val NUM_THREE = 99
  val NUM_FOUR = 100
  val NUM_FIVE = 101
  val NUM_SIX = 102
  val NUM_SEVEN = 103
  val NUM_EIGHT = 104
  val NUM_NINE = 105
  val NUM_MULTIPLY = 106
  val NUM_PLUS = 107
  val NUM_MINUS = 109
  val NUM_PERIOD = 110
  val NUM_DIVISION = 111
  val F1 = 112
  val F2 = 113
  val F3 = 114
  val F4 = 115
  val F5 = 116
  val F6 = 117
  val F7 = 118
  val F8 = 119
  val F9 = 120
  val F10 = 121
  val F11 = 122
  val F12 = 123
  val NUMLOCK = 144
  val SEMICOLON = 186      // needs localization
  val DASH = 189          // needs localization
  val EQUALS = 187      // needs localization
  val COMMA = 188        // needs localization
  val PERIOD = 190      // needs localization
  val SLASH = 191        // needs localization
  val APOSTROPHE = 192    // needs localization
  val SINGLE_QUOTE = 222      // needs localization
  val OPEN_SQUARE_BRACKET = 219 // needs localization
  val BACKSLASH = 220      // needs localization
  val CLOSE_SQUARE_BRACKET = 221 // needs localization
  val WIN_KEY = 224
  val MAC_FF_META = 224 // Firefox (Gecko) fires this for the meta key instead of 91
  val WIN_IME = 229

  // We've seen users whose machines fire this keycode at regular one
  // second intervals. The common thread among these users is that
  // they're all using Dell Inspiron laptops so we suspect that this
  // indicates a hardware/bios problem.
  // http://en.community.dell.com/support-forums/laptop/f/3518/p/19285957/19523128.aspx
  val PHANTOM = 255
  
  /**
   * Returns true if the event contains a text modifying key.
   * @param e A key event.
   * @return Whether it's a text modifying key.
   */
  def isTextModifyingKeyEvent (e: BrowserEvent): Boolean = false
    
}
