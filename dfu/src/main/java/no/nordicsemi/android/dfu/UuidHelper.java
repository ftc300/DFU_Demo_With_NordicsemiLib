package no.nordicsemi.android.dfu;

/* package */ class UuidHelper {

	/* package */ static void assignCustomUuids() {

			SecureDfuImpl.DFU_SERVICE_UUID       = SecureDfuImpl.DEFAULT_DFU_SERVICE_UUID;
			SecureDfuImpl.DFU_CONTROL_POINT_UUID = SecureDfuImpl.DEFAULT_DFU_CONTROL_POINT_UUID;
			SecureDfuImpl.DFU_PACKET_UUID        = SecureDfuImpl.DEFAULT_DFU_PACKET_UUID;
	}
}
