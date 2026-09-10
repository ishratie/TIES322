class Frame {
    public static byte[] build(int seq, byte[] data) {
        byte[] payload = new byte[1 + data.length];
        payload[0] = (byte) seq;
        System.arraycopy(data, 0, payload, 1, data.length);
        byte crc = CRC8.compute(payload, payload.length);
        byte[] frame = new byte[payload.length + 1];
        System.arraycopy(payload, 0, frame, 0, payload.length);
        frame[payload.length] = crc;
        return frame;
    }

    public static boolean isValid(byte[] frame, int len) {
        if (len < 2) return false;
        byte[] payload = new byte[len - 1];
        System.arraycopy(frame, 0, payload, 0, len - 1);
        byte expectedCrc = CRC8.compute(payload, payload.length);
        byte actualCrc = frame[len - 1];
        return expectedCrc == actualCrc;
    }

    public static int getSeq(byte[] frame) {
        return frame[0] & 0xFF;
    }

    public static byte[] getData(byte[] frame, int len) {
        byte[] data = new byte[len - 2];
        System.arraycopy(frame, 1, data, 0, len - 2);
        return data;
    }
}