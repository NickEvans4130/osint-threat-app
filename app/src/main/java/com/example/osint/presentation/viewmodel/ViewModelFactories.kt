package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.osint.domain.usecase.*

class UrlScannerViewModelFactory(
    private val scanUrlUseCase: ScanUrlUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UrlScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UrlScannerViewModel(scanUrlUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class IpScannerViewModelFactory(
    private val scanIpUseCase: ScanIpUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IpScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IpScannerViewModel(scanIpUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HashScannerViewModelFactory(
    private val scanHashUseCase: ScanHashUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HashScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HashScannerViewModel(scanHashUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class QRScannerViewModelFactory(
    private val scanUrlUseCase: ScanUrlUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QRScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QRScannerViewModel(scanUrlUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class FeedStatusViewModelFactory(
    private val getFeedStatusUseCase: GetFeedStatusUseCase,
    private val refreshFeedsUseCase: RefreshFeedsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedStatusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedStatusViewModel(getFeedStatusUseCase, refreshFeedsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NetworkScannerViewModelFactory(
    private val scanLocalNetworkUseCase: ScanLocalNetworkUseCase,
    private val repository: com.example.osint.data.repository.NetworkScannerRepository,
    private val deviceRepository: com.example.osint.data.repository.DeviceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NetworkScannerViewModel(scanLocalNetworkUseCase, repository, deviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MetadataInspectorViewModelFactory(
    private val parseImageMetadataUseCase: ParseImageMetadataUseCase,
    private val stripImageExifUseCase: StripImageExifUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetadataInspectorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetadataInspectorViewModel(parseImageMetadataUseCase, stripImageExifUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NetworkMapViewModelFactory(
    private val getScannedDevicesUseCase: GetScannedDevicesUseCase,
    private val loadOUIDatabaseUseCase: LoadOUIDatabaseUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NetworkMapViewModel(getScannedDevicesUseCase, loadOUIDatabaseUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DeviceDetailViewModelFactory(
    private val getDeviceDetailsUseCase: GetDeviceDetailsUseCase,
    private val ipAddress: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceDetailViewModel(getDeviceDetailsUseCase, ipAddress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
