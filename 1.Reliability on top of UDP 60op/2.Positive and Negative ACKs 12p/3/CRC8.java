class CRC8 {
    //error checker
    public static byte compute(byte[] data, int len) {
        int crc = 0x00;
        for (int i = 0; i < len; i++) {
            crc ^= (data[i] & 0xFF);
            for (int b = 0; b < 8; b++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ 0x07;
                } else {
                    crc = crc << 1;
                }
                crc &= 0xFF;
            }
        }
        return (byte) crc;
    }
}