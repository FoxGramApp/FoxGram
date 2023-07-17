package org.telegram.messenger;

import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import it.foxgram.android.MessageHelper;

public class MessageCustomParamsHelper {

    public static boolean isEmpty(TLRPC.Message message) {
        return message.voiceTranscription == null &&
                !message.voiceTranscriptionOpen &&
                !message.voiceTranscriptionFinal &&
                !message.voiceTranscriptionRated &&
                !message.voiceTranscriptionForce &&
                message.voiceTranscriptionId == 0 &&
                !message.premiumEffectWasPlayed &&
                message.originalLanguage == null &&
                message.translatedToLanguage == null &&
                message.translatedText == null &&
                message.translatedPoll == null &&
                message.originalPoll == null &&
                message.translatedReplyMarkupRows == null &&
                message.originalReplyMarkupRows == null &&
                message.translationProvider == 0;
    }

    public static void copyParams(TLRPC.Message fromMessage, TLRPC.Message toMessage) {
        toMessage.voiceTranscription = fromMessage.voiceTranscription;
        toMessage.voiceTranscriptionOpen = fromMessage.voiceTranscriptionOpen;
        toMessage.voiceTranscriptionFinal = fromMessage.voiceTranscriptionFinal;
        toMessage.voiceTranscriptionForce = fromMessage.voiceTranscriptionForce;
        toMessage.voiceTranscriptionRated = fromMessage.voiceTranscriptionRated;
        toMessage.voiceTranscriptionId = fromMessage.voiceTranscriptionId;
        toMessage.premiumEffectWasPlayed = fromMessage.premiumEffectWasPlayed;
        toMessage.originalLanguage = fromMessage.originalLanguage;
        toMessage.translatedToLanguage = fromMessage.translatedToLanguage;
        toMessage.translatedText = fromMessage.translatedText;
        toMessage.translatedPoll = fromMessage.translatedPoll;
        toMessage.originalPoll = fromMessage.originalPoll;
        toMessage.translatedReplyMarkupRows = fromMessage.translatedReplyMarkupRows;
        toMessage.originalReplyMarkupRows = fromMessage.originalReplyMarkupRows;
        toMessage.translationProvider = fromMessage.translationProvider;
    }


    public static void readLocalParams(TLRPC.Message message, NativeByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        int version = byteBuffer.readInt32(true);
        TLObject params;
        switch (version) {
            case 1:
                params = new Params_v1(message);
                break;
            default:
                throw new RuntimeException("can't read params version = " + version);
        }
        params.readParams(byteBuffer, true);
    }

    public static NativeByteBuffer writeLocalParams(TLRPC.Message message) {
        if (isEmpty(message)) {
            return null;
        }
        TLObject params = new Params_v1(message);
        try {
            NativeByteBuffer nativeByteBuffer = new NativeByteBuffer(params.getObjectSize());
            params.serializeToStream(nativeByteBuffer);
            return nativeByteBuffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class Params_v1 extends TLObject {

        private final static int VERSION = 1;
        final TLRPC.Message message;
        int flags = 0;

        private Params_v1(TLRPC.Message message) {
            this.message = message;
            flags += message.voiceTranscription != null ? 1 : 0;
            flags += message.voiceTranscriptionForce ? 2 : 0;

            flags += message.originalLanguage != null ? 4 : 0;
            flags += message.translatedToLanguage != null ? 8 : 0;
            flags += message.translatedText != null ? 16 : 0;
            flags += message.translatedPoll != null ? 32 : 0;
            flags += message.originalPoll != null ? 64 : 0;
            flags += message.translatedReplyMarkupRows != null ? 128 : 0;
            flags += message.originalReplyMarkupRows != null ? 256 : 0;
            flags += message.translationProvider != 0 ? 512 : 0;
        }

        @Override
        public void serializeToStream(AbstractSerializedData stream) {
            stream.writeInt32(VERSION);
            flags = message.voiceTranscriptionForce ? (flags | 2) : (flags &~ 2);
            stream.writeInt32(flags);
            if ((flags & 1) != 0) {
                stream.writeString(message.voiceTranscription);
            }
            stream.writeBool(message.voiceTranscriptionOpen);
            stream.writeBool(message.voiceTranscriptionFinal);
            stream.writeBool(message.voiceTranscriptionRated);
            stream.writeInt64(message.voiceTranscriptionId);

            stream.writeBool(message.premiumEffectWasPlayed);

            if ((flags & 4) != 0) {
                stream.writeString(message.originalLanguage);
            }
            if ((flags & 8) != 0) {
                stream.writeString(message.translatedToLanguage);
            }
            if ((flags & 16) != 0) {
                message.translatedText.serializeToStream(stream);
            }
            if ((flags & 32) != 0) {
                message.translatedPoll.serializeToStream(stream);
            }
            if ((flags & 64) != 0) {
                message.originalPoll.serializeToStream(stream);
            }
            if ((flags & 128) != 0) {
                message.translatedReplyMarkupRows.serializeToStream(stream);
            }
            if ((flags & 256) != 0) {
                message.originalReplyMarkupRows.serializeToStream(stream);
            }
            if ((flags & 512) != 0) {
                stream.writeInt32(message.translationProvider);
            }
        }

        @Override
        public void readParams(AbstractSerializedData stream, boolean exception) {
            flags = stream.readInt32(true);
            if ((flags & 1) != 0) {
                message.voiceTranscription = stream.readString(exception);
            }
            message.voiceTranscriptionForce = (flags & 2) != 0;
            message.voiceTranscriptionOpen = stream.readBool(exception);
            message.voiceTranscriptionFinal = stream.readBool(exception);
            message.voiceTranscriptionRated = stream.readBool(exception);
            message.voiceTranscriptionId = stream.readInt64(exception);

            message.premiumEffectWasPlayed = stream.readBool(exception);

            if ((flags & 4) != 0) {
                message.originalLanguage = stream.readString(exception);
            }
            if ((flags & 8) != 0) {
                message.translatedToLanguage = stream.readString(exception);
            }
            if ((flags & 16) != 0) {
                message.translatedText = TLRPC.TL_textWithEntities.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            if ((flags & 32) != 0) {
                message.translatedPoll = MessageHelper.PollTexts.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            if ((flags & 64) != 0) {
                message.originalPoll = MessageHelper.PollTexts.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            if ((flags & 128) != 0) {
                message.translatedReplyMarkupRows = MessageHelper.ReplyMarkupButtonsTexts.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            if ((flags & 256) != 0) {
                message.originalReplyMarkupRows = MessageHelper.ReplyMarkupButtonsTexts.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            if ((flags & 512) != 0) {
                message.translationProvider = stream.readInt32(exception);
            }
        }

    }
}
