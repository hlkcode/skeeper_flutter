package com.hippo.skeeper_flutter

class Message {
    companion object {

        /** Message ID */
        const val ID_AUDIO = 0xA1

        /** Message ACTION */
        const val ACTION_OPEN = 0x03
        const val ACTION_CLOSE = 0x04

        /** Message PARAM */
        const val PARAM_MODE_HEART = 0x01
        const val PARAM_MODE_FETUS = 0x02
        const val PARAM_MODE_LUNG = 0x03

        /** Interface 1.1.0 */
        const val PARAM_MODE_HEART2 = 0x00
        const val PARAM_MODE_FETUS2 = 0x01
        const val PARAM_MODE_LUNG2 = 0x02

        /** Response Message ERROR Codes */
        const val ERR_OK = 0x00
        const val ERR_INVALID = 0x01

        /** Notification Message ID */
        const val NOITF_ID_AUDIO = 0x00

        /** Notification Audio Status */
        const val STATUS_AUDIO_NONE = 0x00
        const val STATUS_AUDIO_STOPPED = 0x01

    }
}