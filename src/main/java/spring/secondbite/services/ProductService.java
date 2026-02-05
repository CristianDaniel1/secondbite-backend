package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spring.secondbite.dtos.PageResponseDto;
import spring.secondbite.dtos.products.ProductDetailResponseDto;
import spring.secondbite.dtos.products.ProductDto;
import spring.secondbite.dtos.products.ProductResponseDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.Product;
import spring.secondbite.entities.enums.Category;
import spring.secondbite.exceptions.NotAllowedException;
import spring.secondbite.exceptions.NotFoundException;
import spring.secondbite.mappers.ProductMapper;
import spring.secondbite.repositories.ProductRepository;
import spring.secondbite.repositories.ReviewRepository;
import spring.secondbite.repositories.specs.ProductSpecs;
import spring.secondbite.security.SecurityService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
    private final ReviewRepository reviewRepository;

    private final MarketerService marketerService;
    private final ProductMapper mapper;

    private final FileStorageService fileStorageService;
    private final SecurityService securityService;

    public PageResponseDto<ProductResponseDto> findAllProducts(
            String name,
            Category category,
            int page,
            int limit) {
        Specification<Product> filters = getFilters(name, category);
        Pageable pageRequest = PageRequest.of(page, limit);

        Page<Product> productPage = repository.findAll(filters, pageRequest);
        List<ProductResponseDto> content = productPage.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                content
        );
    }

    public ProductDetailResponseDto findProductById(UUID id) {
        Product product = findProductOrThrow(id);
        Marketer marketer = product.getMarketer();
        UUID marketerId = marketer.getId();

        Double averageRating = reviewRepository.getAverageRating(marketerId);
        ProductResponseDto productResponseDto = mapper.toResponseDto(product);

        return mapper.toDetailResponseDto(productResponseDto, marketerId,
                marketer.getUser().getName(), marketer.getStallName(), averageRating);
    }

    public List<ProductResponseDto> findProductsByMarketer(UUID id) {
        Marketer marketer = marketerService.findOptionalMarketer(id);
        List<Product> products = repository.findAllByMarketer(marketer);

        return products.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto createProduct(ProductDto dto, List<MultipartFile> imageFiles) {
        AppUser user = securityService.getLoggedUserOrThrow();
        Marketer marketer = marketerService.findMarketerByUser(user);

        Product product = mapper.toEntity(dto);
        product.setMarketer(marketer);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> filenames = imageFiles.stream()
                    .map(fileStorageService::saveFile)
                    .collect(Collectors.toList());
            product.setImages(filenames);
        }

        Product saved = repository.save(product);
        return mapper.toResponseDto(saved);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductDto dto, List<MultipartFile> imageFiles) {
        Product existingProduct = findProductOrThrow(productId);
        checkProductIsFromMarketer(existingProduct);

        mapper.updateFromDto(dto, existingProduct);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> newFilenames = imageFiles.stream()
                    .map(fileStorageService::saveFile)
                    .toList();
            existingProduct.getImages().addAll(newFilenames);
        }

        Product updated = repository.save(existingProduct);
        return mapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        checkProductIsFromMarketer(product);
        repository.delete(product);
    }

    public void checkProductIsFromMarketer(Product product) {
        AppUser user = securityService.getLoggedUserOrThrow();
        Marketer marketer = marketerService.findMarketerByUser(user);
        if (marketer.getId() != product.getMarketer().getId())
            throw new NotAllowedException("Ação não permitida: O produto não lhe pertence.");
    }

    public Product findProductOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado."));
    }

    private Specification<Product> getFilters(String name, Category category) {
        Specification<Product> specs = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank())
            specs = specs.and(ProductSpecs.nameLike(name));
        if (category != null)
            specs = specs.and(ProductSpecs.categoryEqual(category));

        return specs;
    }
}
